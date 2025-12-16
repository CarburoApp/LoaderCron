package com.inggarciabaldo.carburo.scheduler.jobs;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.inggarciabaldo.carburo.application.model.EstacionDeServicio;
import com.inggarciabaldo.carburo.application.rest.GasStationHttpRequest;
import com.inggarciabaldo.carburo.application.rest.dto.EETTReqResParserDTO;
import com.inggarciabaldo.carburo.application.rest.dto.ESParserDTO;
import com.inggarciabaldo.carburo.application.rest.parser.EETTReqResParser;
import com.inggarciabaldo.carburo.config.cache.ApplicationCache;
import com.inggarciabaldo.carburo.config.parser.deserialize.ESParserDTODeserializer;
import com.inggarciabaldo.carburo.config.persistencia.jdbc.Jdbc;
import com.inggarciabaldo.carburo.util.email.JobEmailSender;
import com.inggarciabaldo.carburo.util.log.Loggers;
import com.inggarciabaldo.carburo.util.properties.PropertyLoader;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.inggarciabaldo.carburo.config.parser.api.ResponseKeys.API_KEY_RESP_LISTADO_EESS;
import static com.inggarciabaldo.carburo.config.parser.api.ResponseKeys.API_KEY_RESP_RES_CONSULTA;

/**
 * Job de Quartz que se ejecuta periódicamente para:
 * 1. Obtener todas las estaciones mediante GasStationHttpRequest.
 * 2. Parsear cada EESS y sus precios
 * 3. Persistir en la base de datos usando JDBC aquellos datos que resulten convenientes.
 * - Registra tiempos y genera resumen final
 * <p>
 * La ejecución está protegida para que cualquier error no afecte al cron.
 */
public class EESSRecolectorJobParser implements Job {

	/**
	 * Constantes
	 */
	public static final String LOG_ETIQUETA_INICIAL_CRON_ANULADO = "CRON ANULADO: ";
	public static final String LOG_ETIQUETA_INICIAL_FALLO_GUARDADO_JSON_FILE = "Fallo en el guardado de archivo. ";

	/**
	 * Utilidades de control
	 */
	// Logger específico para la aplicación y para el cron
	private static final Logger loggerCron = Loggers.CRON;


	/**
	 * Datos de ejecución
	 */
	private final DatoDeEjecucion datoDeEjecucion = new DatoDeEjecucion();

	/**
	 * Job principal que se ejecuta periódicamente.
	 * El objetivo de este Job es realizar una llamada a una api, recuperar sus datos,
	 * compararlos con los registrados en base de datos y actualizarlos o añadirlos si es necesario.
	 * <p>
	 * Para ello el sistema seguirá los siguientes pasos:
	 * 1. Abrir una conexión con la BD para realizar comprobar si se pueden realizar las operaciones necesarias.
	 * 2. Realizar la petición a la API externa para obtener los datos, válidar la respuesta y almacenar la respuesta.
	 * * En caso de que alguno de los puntos anteriores falle se suspende el cron.
	 * 3. Extraer todos los datos del JSON a los DTOs correspondientes
	 * 4. Comenzar el parseo de los metadatos obtenidos de la API.
	 * 5. Por cada elemento dentro del JSON.
	 * 		5.1. Parsear la estación de servicio.
	 *  	5.2. Comprobar si la estación ya existe en BD. En caso contrario se persistirá.
	 *  	5.3. Si la estación ya existe, comprobar si alguno de sus datos ha cambiado.
	 *  	5.4. Una vez la estación en la BD esté actualizada, proceder a parsear sus precios.
	 *  	5.5. Por cada precio parseado, comprobar si ya existe en BD el dato diario, en caso contrario persistir este.
	 *  6. Una vez todas las estaciones y precios hayan sido parseados y persistidos, cerrar la conexión con la BD.
	 *  7. Registrar en el log el resumen de la ejecución del cron.
	 *  8. Enviar el informe vía email a los administradores del sistema.
	 *  9. Finalizar la ejecución del cron.
	 *
	 * @param context Contexto de ejecución del Job proporcionado por Quartz
	 */
	@Override
	public void execute(JobExecutionContext context) {
		// Fecha de inicio de ejecución
		datoDeEjecucion.fechaInicioEjecucion = LocalDateTime.now();

		//Configuración del logger especifico.
		FileAppender<ILoggingEvent> cronAppender = Loggers.createCronExecutionAppender(
				datoDeEjecucion.fechaInicioEjecucion);
		Logger logger = Loggers.CRON;
		logger.addAppender(cronAppender);
		logger = Loggers.GENERAL;
		logger.addAppender(cronAppender);
		logger = Loggers.DB;
		logger.addAppender(cronAppender);
		logger = Loggers.PARSE;
		logger.addAppender(cronAppender);

		loggerCron.info("<<< Activación del CRON >>> Hora de inicio: {}",
						datoDeEjecucion.formatoHora(
								datoDeEjecucion.fechaInicioEjecucion));

		long tiempoInicioCron = System.currentTimeMillis();

		// 0. Recargar las properties del sistema
		PropertyLoader.getInstance().reloadProperties();

		/*
		 * 1. Abrir una conexión con la BD para realizar comprobar si se pueden realizar las operaciones necesarias.
		 */
		try {
			comprobarConexionConLaBD(); // Si falla lo registra y lanza excepción.
		} catch (Throwable e) {
			return;
		}

		/*
		 * 2. Realizar la petición a la API externa para obtener los datos, válidar la respuesta y almacenar la respuesta.
		 *  En caso de que alguno de los puntos anteriores falle se suspende el cron.
		 */

		JSONObject jsonRespuestaAPI;
		// Hacemos la petición a la API y recuperamos el JSON de respuesta.
		try {
			jsonRespuestaAPI = doAPIReqAndIsRespOK(); // Si falla lo registra y lanza excepción.
		} catch (IllegalStateException e) {
			loggerCron.error(LOG_ETIQUETA_INICIAL_CRON_ANULADO +
									 "Error al extraer los datos JSON de estaciones a un DTO: {}",
							 e.getMessage(), e);
			return;
		}

		/*
		 * 3. Extraer todos los datos del JSON a los DTOs correspondientes
		 */
		//Defino el DTO de la petición
		EETTReqResParserDTO apiRequestDto;
		try {
			apiRequestDto = extraeDatosDesdeJSONaDTO(jsonRespuestaAPI);
		} catch (JsonSyntaxException e) {
			return;
		}
		if (this.datoDeEjecucion.totalEESSEnDTO == 0) {
			return;
		}

		/*
		 * 4. Comenzar el parseo de los metadatos obtenidos de la API.
		 *	 5. Por cada elemento dentro del JSON.
		 *	 		5.1. Parsear la estación de servicio.
		 *	  	5.2. Comprobar si la estación ya existe en BD. En caso contrario se persistirá.
		 *				 	5.3. Si la estación ya existe, comprobar si alguno de sus datos ha cambiado.
		 *				 	5.4. Una vez la estación en la BD esté actualizada, proceder a parsear sus precios.
		 *	  	5.5. Por cada precio parseado, comprobar si ya existe en BD el dato diario, en caso contrario persistir este.
		 */
		List<EstacionDeServicio> listadoDeEESSObtenidas = doESDtoParseToES(apiRequestDto);
		if (listadoDeEESSObtenidas == null ||
				this.datoDeEjecucion.totalEESSParseadas == 0) {
			loggerCron.warn(LOG_ETIQUETA_INICIAL_CRON_ANULADO +
									"No se parseó ninguna estación correctamente. CRON finalizado.");
			return;
		}

		// Persistencia
		try {
			ProcesadoDePersistenciaEESSaBD procesador;
			loggerCron.info("Comienza la fase de persistencia.");
			procesador = new ProcesadoDePersistenciaEESSaBD(listadoDeEESSObtenidas,
															datoDeEjecucion);
			loggerCron.info("Objeto de procesamiento creado correctamente.");
			long tiempoInicioPersistencia = System.currentTimeMillis();
			procesador.procesar();
			this.datoDeEjecucion.tiempoPersistenciaMs =
					System.currentTimeMillis() - tiempoInicioPersistencia;
		} catch (Exception e) {
			loggerCron.info(LOG_ETIQUETA_INICIAL_CRON_ANULADO +
									"Error inesperado en la fase de persistencia: {}",
							e.getMessage(), e);
			return;
		}

		/*
		 * 6. Una vez todas las estaciones y precios hayan sido parseados y persistidos, cerrar la conexión con la BD.
		 * 7. Registrar en el log el resumen de la ejecución del cron.
		 * 8. Enviar el informe vía email a los administradores del sistema.
		 * 9. Finalizar la ejecución del cron.
		 */
		this.datoDeEjecucion.tiempoTotalCron =
				System.currentTimeMillis() - tiempoInicioCron;
		// Dado que es el fin del ciclo de vida limpiamos cualquier recurso abierto
		ApplicationCache.instance.clearCache();
		finEjecucionCronYRegistroInformeEjecucion();

		// Desacoplamos la redirección de los logs.
		cronAppender.stop();
		logger = Loggers.CRON;
		logger.detachAppender(cronAppender);
		logger = Loggers.GENERAL;
		logger.detachAppender(cronAppender);
		logger = Loggers.DB;
		logger.detachAppender(cronAppender);
		logger = Loggers.PARSE;
		logger.detachAppender(cronAppender);
	}

	/**
	 * Comprueba la conexión con la base de datos.
	 * Si no se puede conectar, lanza una excepción para cancelar el cron.
	 */
	private void comprobarConexionConLaBD() throws Throwable {
		if (Jdbc.testConnection()) return;
		// Si no se puede conectar a la BD, cancelar el cron
		loggerCron.error(LOG_ETIQUETA_INICIAL_CRON_ANULADO +
								 "no se pudo establecer conexión con la base de datos.");
		throw new IOException("No se pudo establecer conexión con la base de datos.");
	}


	/**
	 * Claves para las properties de configuración de la llamada a la API
	 */
	private static final String PROP_MAX_REINTENTOS = "cron.httpReq.intentos";
	private static final String PROP_TIEMPO_SLEEP_MS = "cron.httpReq.tiempoMiliSegundos.entre.intentos";

	/**
	 * Valores por defecto si no están configuradas las properties
	 */
	private static final int DEFAULT_MAX_REINTENTOS = 3;
	private static final long DEFAULT_SLEEP_MS = 20_000L; // 20 segundos

	/**
	 * Obtiene el JSON de estaciones desde la API externa, validando la respuesta.
	 * Implementa reintentos configurables desde properties.
	 *
	 * @return JSONObject con la respuesta de la API
	 * @throws IllegalStateException Si no se obtiene una respuesta válida tras todos los intentos
	 */
	private JSONObject doAPIReqAndIsRespOK() throws IllegalStateException {
		// Leemos la configuración desde las properties (como String) y convertimos a tipos correctos
		PropertyLoader loader = PropertyLoader.getInstance();
		int maxIntentos = Integer.parseInt(
				loader.getApplicationProperty(PROP_MAX_REINTENTOS,
											  String.valueOf(DEFAULT_MAX_REINTENTOS)));

		long sleepMs = Long.parseLong(loader.getApplicationProperty(PROP_TIEMPO_SLEEP_MS,
																	String.valueOf(
																			DEFAULT_SLEEP_MS)));

		for (int nIntento = 1; nIntento <= maxIntentos; nIntento++) {
			long inicioPeticion = System.currentTimeMillis();
			try {
				loggerCron.info("SOLICITUD en curso de datos a la API. Intento {} de {}.",
								nIntento, maxIntentos);

				// Realiza la petición HTTP a la API externa
				GasStationHttpRequest request = new GasStationHttpRequest();
				JSONObject respuestaAPI = request.getAllStations();

				// Calculamos el tiempo de respuesta
				this.datoDeEjecucion.tiempoPeticionApiMs =
						System.currentTimeMillis() - inicioPeticion;

				// Validamos la estructura de la respuesta
				if (respuestaAPI.keySet().contains(API_KEY_RESP_RES_CONSULTA) &&
						respuestaAPI.keySet().contains(API_KEY_RESP_RES_CONSULTA) &&
						respuestaAPI.keySet().contains(API_KEY_RESP_LISTADO_EESS)) {

					// Guardamos información adicional para seguimiento
					this.datoDeEjecucion.totalEESSEnJson = respuestaAPI.getJSONArray(
							API_KEY_RESP_LISTADO_EESS).length();

					// Guardamos la respuesta en archivo para auditoría/debug
					guardarRespuestaAPIEnArchivo(respuestaAPI);

					loggerCron.info("Respuesta API válida obtenida en {} ms.",
									this.datoDeEjecucion.tiempoPeticionApiMs);
					return respuestaAPI;
				}

				// Si llegamos aquí, la respuesta no es válida
				IllegalStateException e = new IllegalStateException(
						LOG_ETIQUETA_INICIAL_CRON_ANULADO +
								"ERROR al VALIDAR la RESPUESTA JSON de la API.");
				loggerCron.error(e.getMessage());
				throw e;

			} catch (Exception e) {
				loggerCron.error(LOG_ETIQUETA_INICIAL_CRON_ANULADO +
										 "ERROR al REALIZAR la PETICIÓN HTTP a la API en el intento número {}: {}",
								 nIntento, e.getMessage(), e);

				// Si no hemos alcanzado el máximo de reintentos, dormimos antes de reintentar
				if (nIntento < maxIntentos) {
					loggerCron.info(
							"El SISTEMA DUERME {} ms antes de REINTENTAR la PETICIÓN.",
							sleepMs);
					try {
						Thread.sleep(sleepMs);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						throw new IllegalStateException(
								"El hilo fue interrumpido durante el sleep de reintento.",
								ie);
					}
				}
			}
		}
		// Si ya superamos los reintentos, lanzamos excepción final
		throw new IllegalStateException(LOG_ETIQUETA_INICIAL_CRON_ANULADO +
												"NO se pudo obtener una RESPUESTA válida de la API tras " +
												maxIntentos + " intentos.");
	}

	/**
	 * Extrae los datos del JSON de respuesta a un DTO usando Gson.
	 *
	 * @param jsonRespuestaAPI JSON de respuesta de la API
	 * @return El DTO con los datos extraídos
	 */
	private EETTReqResParserDTO extraeDatosDesdeJSONaDTO(JSONObject jsonRespuestaAPI)
			throws JsonSyntaxException {
		long tiempoInicioEjecucion = System.currentTimeMillis();
		// Crear instancia de Gson
		Gson gson = new GsonBuilder().registerTypeAdapter(ESParserDTO.class,
														  new ESParserDTODeserializer())
				.create();
		// Mapear JSON a DTO
		EETTReqResParserDTO dto = gson.fromJson(jsonRespuestaAPI.toString(),
												EETTReqResParserDTO.class);
		this.datoDeEjecucion.tiempoJSONParseoDTOMs =
				System.currentTimeMillis() - tiempoInicioEjecucion;
		this.datoDeEjecucion.totalEESSEnDTO        = dto.getListaEESS().size();
		if (this.datoDeEjecucion.totalEESSEnDTO == 0) loggerCron.warn(
				LOG_ETIQUETA_INICIAL_CRON_ANULADO +
														 "No se transformado ninguna estación a DTO. CRON finalizado.");
		return dto;
	}


	/**
	 * Parsea las estaciones de servicio desde el DTO obtenido del JSON de la API.
	 *
	 * @param apiRequestDto DTO con los datos de la API
	 * @return Lista de estaciones de servicio parseadas
	 */
	private List<EstacionDeServicio> doESDtoParseToES(EETTReqResParserDTO apiRequestDto) {
		// Defino la respuesta del parser
		List<EstacionDeServicio> estacionesDeServicioParseadas = null;
		// Instanciamos el parser y lanzamos el parseo
		try {
			EETTReqResParser parser = new EETTReqResParser(apiRequestDto,
														   datoDeEjecucion);
			{ //Medimos tiempos
				long tiempoInicioParseo = System.currentTimeMillis();
				estacionesDeServicioParseadas = parser.parse();
				this.datoDeEjecucion.tiempoParseoEESSMs =
						System.currentTimeMillis() - tiempoInicioParseo;
			}
			datoDeEjecucion.setFechaDeParser(parser.getFechaDeParser());
		} catch (IllegalArgumentException | IllegalStateException e) {
			loggerCron.error(LOG_ETIQUETA_INICIAL_CRON_ANULADO +
									 "Ha ocurrido un error en el parseo de las EESS: {}",
							 e.getMessage(), e);
		} catch (Exception e) {
			loggerCron.error(LOG_ETIQUETA_INICIAL_CRON_ANULADO +
									 "Ha ocurrido un error inesperado en el parseo de las EESS: {}",
							 e.getMessage(), e);
		}
		this.datoDeEjecucion.totalEESSParseadas = (estacionesDeServicioParseadas ==
										   null) ? 0 : estacionesDeServicioParseadas.size();
		return estacionesDeServicioParseadas;
	}


	/**
	 * Guarda la respuesta JSON de la API en un archivo local con formato legible.
	 * La ubicación del archivo se puede configurar mediante properties.
	 * Si no se configura, se guarda en la ubicación por defecto (directorio actual).
	 *
	 * @param jsonRespuestaAPI JSON de respuesta de la API
	 */
	private void guardarRespuestaAPIEnArchivo(JSONObject jsonRespuestaAPI) {
		// Obtener ruta desde properties, "Ejecuciones" por defecto
		String rutaDirectorio = PropertyLoader.getInstance()
				.getApplicationProperty("cron.api.requests.filepath", "files").trim();

		// Generamos un nombre de archivo único con timestamp
		String timestamp = LocalDateTime.now()
				.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
		String nombreArchivo = "EESS_Response_" + timestamp + ".json";

		if (rutaDirectorio.isEmpty()) {
			// Si no hay ruta, guardamos en el directorio actual
			this.datoDeEjecucion.jsonRespuestaArchivo = new File(nombreArchivo);
		} else {
			// Si hay ruta, nos aseguramos que exista la carpeta
			File carpeta = new File(rutaDirectorio);
			if (!carpeta.exists()) {
				try {
					if (!carpeta.mkdirs()) loggerCron.warn(
							LOG_ETIQUETA_INICIAL_FALLO_GUARDADO_JSON_FILE +
									"No se pudo crear el directorio: {}", rutaDirectorio);
					return;
				} catch (SecurityException e) {
					loggerCron.warn(LOG_ETIQUETA_INICIAL_FALLO_GUARDADO_JSON_FILE +
											"No hay permisos para crear el directorio: {}",
									rutaDirectorio, e);
					return;
				}
			}
			if (!carpeta.isDirectory()) {
				loggerCron.warn(LOG_ETIQUETA_INICIAL_FALLO_GUARDADO_JSON_FILE +
										"La ruta existe pero no es un directorio: {}",
								rutaDirectorio);
				return;
			}
			this.datoDeEjecucion.jsonRespuestaArchivo = new File(carpeta, nombreArchivo);
		}


		try (FileWriter writer = new FileWriter(
				this.datoDeEjecucion.jsonRespuestaArchivo)) {
			writer.write(jsonRespuestaAPI.toString(4)); // identación de 4 espacios
			writer.flush();
			loggerCron.info(
					"Almacenada respuesta de la API en la ubicación designada. Bajo el nombre: {}",
					this.datoDeEjecucion.jsonRespuestaArchivo.getName());
		} catch (IOException e) {
			loggerCron.error(LOG_ETIQUETA_INICIAL_FALLO_GUARDADO_JSON_FILE +
									 "Error al guardar la respuesta de la API en archivo: {}",
							 e.getMessage(), e);
		}
	}

	/**
	 * Registra el informe de la ejecución del cron en el log y envía el correo con el informe.
	 */
	private void finEjecucionCronYRegistroInformeEjecucion() {
		this.datoDeEjecucion.fechaFinEjecucion = LocalDateTime.now();

		// Registro del resumen en el log
		loggerCron.info(this.datoDeEjecucion.getInformeEjecucion());

		//Envío de correo con el informe de la ejecución del
		boolean isReport = false;
		try {
			JobEmailSender emailSender = new JobEmailSender(datoDeEjecucion);
			emailSender.sendReport();
			isReport = true;
		} catch (Exception e) {
			loggerCron.error("Error enviando informe por correo: {}", e.getMessage(), e);
		}

		// Log indicando la finalización del cron y el estado del envío del correo
		loggerCron.info(
				"<<< CRON Finalizado >>> Hora de fin: {}. ¿Se ha enviado el informe por correo? {}.",
				datoDeEjecucion.formatoHora(this.datoDeEjecucion.fechaFinEjecucion),
				isReport ? "Sí" : "No");
	}
}