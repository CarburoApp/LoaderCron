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
import com.inggarciabaldo.carburo.util.email.EmailSender;
import com.inggarciabaldo.carburo.util.email.strategies.CorrectJobExecutionConstructStrategy;
import com.inggarciabaldo.carburo.util.email.strategies.FailedJobExecutionConstructStrategy;
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
 * Job Quartz encargado de la recolección, parseo y persistencia
 * de estaciones de servicio obtenidas desde una API externa.
 *<p>
 * El flujo está diseñado para:
 * - Detectar errores en cada fase
 * - Anular el cron de forma controlada
 * - Registrar trazas completas
 * - Notificar por correo cualquier fallo no planificado
 */
public class EESSRecolectorJobParser implements Job {


	/* =====================================================
	 * =============== CONSTANTES DE LOG ===================
	 * ===================================================== */

	private static final String LOG_CRON_START = "<<< Activación del CRON >>> Hora de inicio: {}";

	private static final String LOG_CRON_END = "<<< CRON Finalizado >>> Hora de fin: {}. ¿Correo enviado? {}.";

	private static final String LOG_CRON_ABORTED = "<<< CRON INTERRUMPIDO POR ERROR NO PLANIFICADO >>>";

	private static final String LOG_PREFIX_ABORT = "CRON ANULADO: ";

	private static final String LOG_DB_CONNECTION_ERROR = "No se pudo establecer conexión con la base de datos.";

	private static final String LOG_API_INVALID_RESPONSE = "Respuesta JSON inválida obtenida desde la API.";

	private static final String LOG_NO_EESS_PARSED = "No se parseó ninguna estación correctamente..";

	private static final String LOG_JSON_SAVE_ERROR = "Fallo en el guardado del archivo JSON de respuesta.";

	private static final String LOG_PERSISTENCE_ERROR = "Error inesperado durante la fase de persistencia.";

	/* =====================================================
	 * ================== CONFIG API =======================
	 * ===================================================== */

	private static final String PROP_MAX_REINTENTOS = "cron.httpReq.intentos";

	private static final String PROP_TIEMPO_SLEEP_MS = "cron.httpReq.tiempoMiliSegundos.entre.intentos";

	private static final int DEFAULT_MAX_REINTENTOS = 3;
	private static final long DEFAULT_SLEEP_MS = 20_000L;

	/* =====================================================
	 * ==================== CAMPOS =========================
	 * ===================================================== */

	private static final Logger loggerCron = Loggers.CRON;
	public static final String EJECUCION_PRINCIPAL_DEL_JOB = "Ejecución principal del Job";
	public static final String LOG_DTO_RECUPERADO_VACIO = "El dto recuperado tiene 0 estaciones de servicio. Se procede a detener el cron.";

	private FileAppender<ILoggingEvent> appender;

	private final DatosDeEjecucion datosEjecucion = new DatosDeEjecucion();

	/* =====================================================
	 * ==================== EXECUTE ========================
	 * ===================================================== */

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
	 * 5.1. Parsear la estación de servicio.
	 * 5.2. Comprobar si la estación ya existe en BD. En caso contrario se persistirá.
	 * 5.3. Si la estación ya existe, comprobar si alguno de sus datos ha cambiado.
	 * 5.4. Una vez la estación en la BD esté actualizada, proceder a parsear sus precios.
	 * 5.5. Por cada precio parseado, comprobar si ya existe en BD el dato diario, en caso contrario persistir este.
	 * 6. Una vez todas las estaciones y precios hayan sido parseados y persistidos, cerrar la conexión con la BD.
	 * 7. Registrar en el log el resumen de la ejecución del cron.
	 * 8. Enviar el informe vía email a los administradores del sistema.
	 * 9. Finalizar la ejecución del cron.
	 *
	 * @param context Contexto de ejecución del Job proporcionado por Quartz
	 */
	@Override
	public void execute(JobExecutionContext context) {
		inicializarEjecucion();
		long inicioCronMs = System.currentTimeMillis();

		try {
			recargarProperties();
			// Abrir una conexión con la BD para realizar comprobar si se pueden realizar las operaciones necesarias.
			comprobarConexionBD();

			// Realizar la petición a la API externa para obtener los datos,
			JSONObject json = obtenerDatosDesdeAPI();
			// Extraer todos los datos del JSON a los DTOs correspondientes
			EETTReqResParserDTO dto = mapearJsonADTO(json);

			if (dto.getListaEESS().isEmpty()) {
				loggerCron.warn(LOG_DTO_RECUPERADO_VACIO);
				datosEjecucion.addWarning(LOG_DTO_RECUPERADO_VACIO);
				finalizarEjecucionCorrecta(inicioCronMs);
				return;
			}

			List<EstacionDeServicio> estaciones = parsearEstaciones(dto);
			persistirEstaciones(estaciones);
			finalizarEjecucionCorrecta(inicioCronMs);
		} catch (Throwable t) {
			anularCronPorErrorNoPlanificado(t);
		} finally {
			liberarRecursos();
		}
	}

	/* =====================================================
	 * =============== MÉTODOS DE FASE =====================
	 * ===================================================== */

	private void inicializarEjecucion() {
		// Fecha de inicio de ejecución
		datosEjecucion.setFechaInicioEjecucion(LocalDateTime.now());

		configurarAppenderLogs(datosEjecucion.getFechaInicioEjecucion());
		loggerCron.info(LOG_CRON_START, datosEjecucion.formatoHora(
				datosEjecucion.getFechaInicioEjecucion()));
	}

	private void recargarProperties() {
		PropertyLoader.getInstance().reloadProperties();
	}

	private void comprobarConexionBD() throws IOException {
		if (!Jdbc.testConnection()) throw new IOException(LOG_DB_CONNECTION_ERROR);
	}

	private JSONObject obtenerDatosDesdeAPI() {
		try {
			return doAPIReqAndIsRespOK();
		} catch (Exception e) {
			throw new IllegalStateException(LOG_API_INVALID_RESPONSE, e);
		}
	}

	private EETTReqResParserDTO mapearJsonADTO(JSONObject json) {
		try {
			return extraeDatosDesdeJSONaDTO(json);
		} catch (JsonSyntaxException e) {
			throw new IllegalStateException("Error parseando JSON a DTO.", e);
		}
	}

	private List<EstacionDeServicio> parsearEstaciones(EETTReqResParserDTO dto) {
		List<EstacionDeServicio> estaciones = doESDtoParseToES(dto);
		if (estaciones == null || estaciones.isEmpty())
			throw new IllegalStateException(LOG_NO_EESS_PARSED);
		return estaciones;
	}

	private void persistirEstaciones(List<EstacionDeServicio> estaciones) {
		try {
			long inicio = System.currentTimeMillis();
			new ProcesadoDePersistenciaEESSaBD(estaciones, datosEjecucion).procesar();
			datosEjecucion.setTiempoTotalPersistenciaMs(
					System.currentTimeMillis() - inicio);
		} catch (Exception e) {
			throw new IllegalStateException(LOG_PERSISTENCE_ERROR, e);
		}
	}

	/* =====================================================
	 * =================== FINALIZACIÓN ====================
	 * ===================================================== */

	private void finalizarEjecucionCorrecta(long inicioCronMs) {
		datosEjecucion.setTiempoTotalCronMs(System.currentTimeMillis() - inicioCronMs);

		datosEjecucion.setFechaFinEjecucion(LocalDateTime.now());
		// Registro del resumen en el log
		loggerCron.info(datosEjecucion.getInformeEjecucion());

		boolean enviado = enviarCorreoEjecucionCorrecta();

		loggerCron.info(LOG_CRON_END,
						datosEjecucion.formatoHora(datosEjecucion.getFechaFinEjecucion()),
						enviado ? "Sí" : "No");
	}

	private boolean enviarCorreoEjecucionCorrecta() {
		try {
			new EmailSender(
					new CorrectJobExecutionConstructStrategy(datosEjecucion)).sendEmail();
			return true;
		} catch (Exception e) {
			loggerCron.error("Error enviando informe de ejecución.", e);
			return false;
		}
	}

	/* =====================================================
	 * ============ ANULACIÓN CONTROLADA ===================
	 * ===================================================== */

	private void anularCronPorErrorNoPlanificado(Throwable causa) {

		datosEjecucion.setFechaFinEjecucion(LocalDateTime.now());

		loggerCron.error(LOG_PREFIX_ABORT + "{}. Motivo: {}",
						 EESSRecolectorJobParser.EJECUCION_PRINCIPAL_DEL_JOB,
						 causa.getMessage(), causa);

		try {
			new EmailSender(new FailedJobExecutionConstructStrategy(datosEjecucion,
																	causa)).sendEmail();
		} catch (Exception e) {
			loggerCron.error("No se pudo enviar el correo de error del cron.", e);
		}

		loggerCron.info(LOG_CRON_ABORTED);
	}

	/* =====================================================
	 * ================= UTILIDADES ========================
	 * ===================================================== */

	private void configurarAppenderLogs(LocalDateTime inicio) {
		appender = Loggers.createCronExecutionAppender(inicio, datosEjecucion);
		Loggers.CRON.addAppender(appender);
		Loggers.GENERAL.addAppender(appender);
		Loggers.DB.addAppender(appender);
		Loggers.PARSE.addAppender(appender);
	}

	private void liberarRecursos() {
		//Liberamos la caché
		ApplicationCache.instance.clearCache();

		// Desacoplamos la redirección de los logs.
		if (appender != null) {
			appender.stop();
			Loggers.CRON.detachAppender(appender);
			Loggers.GENERAL.detachAppender(appender);
			Loggers.DB.detachAppender(appender);
			Loggers.PARSE.detachAppender(appender);
		}
	}


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
				datosEjecucion.setTiempoTotalHttpRequestMs(
						System.currentTimeMillis() - inicioPeticion);

				// Validamos la estructura de la respuesta
				if (respuestaAPI.keySet().contains(API_KEY_RESP_RES_CONSULTA) &&
						respuestaAPI.keySet().contains(API_KEY_RESP_RES_CONSULTA) &&
						respuestaAPI.keySet().contains(API_KEY_RESP_LISTADO_EESS)) {
					datosEjecucion.setApiRespuestaValida(true);
					datosEjecucion.setApiHttpIntentosRealizados(nIntento);
					// Guardamos información adicional para seguimiento
					datosEjecucion.setApiTotalEESSRecibidasJson(
							respuestaAPI.getJSONArray(API_KEY_RESP_LISTADO_EESS)
									.length());
					// Guardamos la respuesta en archivo para auditoría/debug
					guardarRespuestaAPIEnArchivo(respuestaAPI);

					loggerCron.info("Respuesta API válida obtenida en {} ms.",
									datosEjecucion.getTiempoTotalHttpRequestMs());
					return respuestaAPI;
				}

				// Si llegamos aquí, la respuesta no es válida
				IllegalStateException e = new IllegalStateException(
						"ERROR al VALIDAR la RESPUESTA JSON de la API.");
				loggerCron.error(e.getMessage());
				throw e;

			} catch (Exception e) {
				loggerCron.error(
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
		throw new IllegalStateException(
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
		datosEjecucion.setParseoJsonADtoTiempoMs(
				System.currentTimeMillis() - tiempoInicioEjecucion);
		datosEjecucion.setParseoTotalEESSEnDto(dto.getListaEESS().size());
		if (datosEjecucion.getParseoTotalEESSEnDto() == 0) {
			loggerCron.warn(
					"No se transformado ninguna estación a DTO. CRON finalizado.");
			datosEjecucion.addWarning(
					"No se transformado ninguna estación a DTO. CRON finalizado.");
		}
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
			EETTReqResParser parser = new EETTReqResParser(apiRequestDto, datosEjecucion);
			{ //Medimos tiempos
				long tiempoInicioParseo = System.currentTimeMillis();
				estacionesDeServicioParseadas = parser.parse();
				datosEjecucion.setTiempoTotalParseoMs(
						System.currentTimeMillis() - tiempoInicioParseo);
			}
			datosEjecucion.setFechaDeDatosProcesados(
					datosEjecucion.getFechaDeDatosProcesados());
		} catch (IllegalArgumentException | IllegalStateException e) {
			loggerCron.error("Ha ocurrido un error en el parseo de las EESS: {}",
							 e.getMessage(), e);
		} catch (Exception e) {
			loggerCron.error(
					"Ha ocurrido un error inesperado en el parseo de las EESS: {}",
							 e.getMessage(), e);
		}
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
			datosEjecucion.setArchivoRespuestaApiJson(new File(nombreArchivo));
		} else {
			// Si hay ruta, nos aseguramos que exista la carpeta
			File carpeta = new File(rutaDirectorio);
			if (!carpeta.exists()) {
				try {
					if (!carpeta.mkdirs()) {
						loggerCron.warn(LOG_JSON_SAVE_ERROR +
												"No se pudo crear el directorio: {}",
										rutaDirectorio);
						datosEjecucion.addWarning(LOG_DTO_RECUPERADO_VACIO);
					}
					return;
				} catch (SecurityException e) {
					loggerCron.warn(LOG_JSON_SAVE_ERROR +
											"No hay permisos para crear el directorio: {}",
									rutaDirectorio, e);
					return;
				}
			}
			if (!carpeta.isDirectory()) {
				loggerCron.warn(LOG_JSON_SAVE_ERROR +
										"La ruta existe pero no es un directorio: {}",
								rutaDirectorio);
				return;
			}
			datosEjecucion.setArchivoRespuestaApiJson(new File(carpeta, nombreArchivo));
		}


		try (FileWriter writer = new FileWriter(
				datosEjecucion.getArchivoRespuestaApiJson())) {
			writer.write(jsonRespuestaAPI.toString(4)); // identación de 4 espacios
			writer.flush();
			loggerCron.info(
					"Almacenada respuesta de la API en la ubicación designada. Bajo el nombre: {}",
					datosEjecucion.getArchivoRespuestaApiJson().getName());
		} catch (IOException e) {
			loggerCron.error(LOG_JSON_SAVE_ERROR +
									 "Error al guardar la respuesta de la API en archivo: {}",
							 e.getMessage(), e);
		}
	}
}