package com.inggarciabaldo.carburo.application.rest.parser;

import com.inggarciabaldo.carburo.application.Factorias;
import com.inggarciabaldo.carburo.application.model.EstacionDeServicio;
import com.inggarciabaldo.carburo.application.rest.dto.EETTReqResParserDTO;
import com.inggarciabaldo.carburo.application.rest.dto.ESParserDTO;
import com.inggarciabaldo.carburo.application.service.ServiceFactory;
import com.inggarciabaldo.carburo.util.log.Loggers;
import com.inggarciabaldo.carburo.util.properties.PropertyLoader;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de alto nivel para parsear la petición a la api, actualmente introducida en el
 * DTO @{@link EETTReqResParserDTO}. A continuación
 * se procesará para obtener las estaciones de servicio (EESS) y sus precios correspondientes.
 * <p>
 * Este proceso de parseo tiene el uso principal de convertir los actuales DTOs en entidades
 * correctas para posteriormente compararlas y persistirlas en la base de datos.
 * <p>
 * El primer paso será parsear los datos de primer nivel del DTO/JSON recibido.
 * El segundo paso será utilizar la clase @{@link EESSParser} para parsear cada estación de servicio.
 */
public class EETTReqResParser {

	/**
	 * Constantes
	 */
	public static final String DATE_API_RES_FORMAT = "dd/MM/yyyy HH:mm:ss";
	public static final String ETIQUETA_LOGGER = "EETTReqResParser. ";

	/**
	 * Utilidades de control
	 */
	private static final Logger loggerParse = Loggers.PARSE;

	/**
	 * Utilidades de uso en el Job
	 */
	private final PropertyLoader propertyLoader = PropertyLoader.getInstance();

	/**
	 * Datos de ejecución
	 */
	private final LocalDate fechaDeParser;
	private final EETTReqResParserDTO dto;
	private long tiempoCargaDatosInicialesCache;
	private long tiempoDTOParseoEnEntidadesMs;
	private long tiempoPersistenciaMs;
	private long tiempoTotalCron;
	private int totalEESSParseadasCorrectamente;
	private int totalErroresParseEESS = 0;
	private int totalEESSEnJson;
	private int totalEESSEnDTO;
	private int totalEESSParseadas;
	private int totalEESSInsertadas;
	private int totalEESSActualizadas;
	private int totalPreciosEESSInsertados;

	/**
	 * Constructor del parser, que recibe el DTO ya deserializado.
	 *
	 * @param dto DTO recibido de la API ya deserializado.
	 * @throws IllegalArgumentException Si el DTO es nulo o no contiene datos válidos.
	 */
	public EETTReqResParser(EETTReqResParserDTO dto) {
		loggerParse.info(ETIQUETA_LOGGER +
								 "Inicializando el parser de EETTReqResParser para el DTO recibido.");
		if (dto == null) {
			loggerParse.error(ETIQUETA_LOGGER +
									  "El DTO recibido por el Parser es nulo. Se procede a finalizar el parseo.");
			throw new IllegalArgumentException("El DTO recibido por el Parser es nulo.");
		}

		this.dto = dto;

		this.fechaDeParser = parseFecha();
		// Comprobar que la fecha es el mismo día que hoy
		if (!LocalDate.now().equals(fechaDeParser)) {
			throw new IllegalArgumentException("La fecha no corresponde al día actual");
		}
		loggerParse.info(ETIQUETA_LOGGER +
								 "Se ha inicializado correctamente el objeto EETTReqResParser.");
	}

	/**
	 * Parsea los datos de primer nivel del DTO recibido. Invoca al método encargado de
	 * parsear las EESS, yendo al segundo y último nivel de jerarquía en el DTO/JSON recibido.
	 */
	public List<EstacionDeServicio> parse() {
		loggerParse.info(ETIQUETA_LOGGER + "Inicio de parseo del DTO recibido.");
		try {
			return parseListaEESS();
		} catch (IllegalArgumentException e) {
			loggerParse.error(ETIQUETA_LOGGER +
									  "Error en el paseo de los datos de primer nivel del DTO/JSON recibido: {}",
							  e.getMessage(), e);
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Extrae la fecha del DTO. A parte de comprobar su validez debe de ser tranformada a @{@link LocalDate}.
	 *
	 * @throws IllegalArgumentException Si no está presente o no tiene un formato válido.
	 */
	private LocalDate parseFecha() {
		if (dto == null) throw new IllegalArgumentException("El DTO a parsear es nulo.");
		String fecha = dto.getFecha();
		if (fecha == null) throw new IllegalArgumentException(
				"La fecha de la petición a la API no está presente en el DTO recibido.");

		// Formato esperado: "dd/MM/yyyy HH:mm:ss"
		DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(
						"dd/MM/yyyy ")
				.appendValue(ChronoField.HOUR_OF_DAY)      // 0–23 (1 o 2 dígitos)
				.appendLiteral(':').appendValue(ChronoField.MINUTE_OF_HOUR, 2)
				.appendLiteral(':').appendValue(ChronoField.SECOND_OF_MINUTE, 2)
				.toFormatter();

		try {
			LocalDateTime fechaDT = LocalDateTime.parse(fecha, formatter);
			return fechaDT.toLocalDate();
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException(
					"La fecha recibida desde la API no tiene un formato válido: " + fecha,
					e);
		}
	}

	/**
	 * Extrae la fecha del DTO. A parte de comprobar su validez debe de ser tranformada a @{@link LocalDate}.
	 *
	 * @throws IllegalArgumentException Si no está presente o no tiene un formato válido.
	 */
	private List<EstacionDeServicio> parseListaEESS() {
		List<ESParserDTO> listaEESS = dto.getListaEESS();
		if (listaEESS == null) throw new IllegalArgumentException(
				"La lista de estaciones de servicios de la petición a la API no está presente en el DTO recibido.");

		if (listaEESS.isEmpty()) throw new IllegalArgumentException(
				"La lista de estaciones de servicios de la petición a la API está vacía en el DTO recibido.");

		return parseListaEESSADominio(listaEESS);
	}

	/**
	 * Parsea la lista de EESS del DTO a entidades de dominio. Invoca al método encargado
	 * de parsear cada objeto concreto de EESS.
	 *
	 * @param listaEESS Lista de EESS en formato DTO
	 * @return Lista de EESS en formato dominio
	 */
	private List<EstacionDeServicio> parseListaEESSADominio(List<ESParserDTO> listaEESS) {
		// Definimos la lista de salida
		List<EstacionDeServicio> resultado = new ArrayList<>();
		List<ESParserDTO> conFallos = new ArrayList<>();

		// Creamos el parser de EESS con los datos de referencia actuales
		EESSParser eessParser = createANewEESSParser();

		long inicioParseoEESSMs = System.currentTimeMillis();

		// Parseamos cada EESS del DTO recibido
		EstacionDeServicio estacionDeServicio;
		for (ESParserDTO estacionDeServicioDTO : listaEESS) {
			try {
				estacionDeServicio = parseESADominio(eessParser, estacionDeServicioDTO);
			} catch (IllegalArgumentException | IllegalStateException e) {
				loggerParse.error(ETIQUETA_LOGGER +
										  " Ha ocurrido un error al parsear la estación de servicio - Se ignora: {}",
								  estacionDeServicioDTO.toString(), e);
				conFallos.add(estacionDeServicioDTO);
				this.totalErroresParseEESS++;
				continue;
			} catch (Exception e) {
				loggerParse.error(ETIQUETA_LOGGER +
										  " Ha ocurrido un error inesperado al parsear la estación de servicio - Se ignora: {}",
								  estacionDeServicioDTO.toString(), e);
				conFallos.add(estacionDeServicioDTO);
				this.totalErroresParseEESS++;
				continue;
			}
			if (estacionDeServicio != null) resultado.add(estacionDeServicio);
		}

		this.tiempoDTOParseoEnEntidadesMs =
				System.currentTimeMillis() - inicioParseoEESSMs;

		this.totalEESSParseadasCorrectamente = resultado.size();

		loggerParse.info(ETIQUETA_LOGGER +
								 "Fin de parseo del DTO recibido. Se han parseado correctamente {} EESS de las {} indicadas en el DTO inicial. Con errores: {}.",
						 this.totalEESSParseadasCorrectamente, listaEESS.size(),
						 this.totalErroresParseEESS);
		return resultado;
	}

	/**
	 * Crea una nueva instancia de EESSParser con los datos de referencia actuales.
	 * Normalmente, JDBC actuará directamente contra BD, pero estos datos es muy raro que
	 * varíen, por lo que se piden en bloque al inicio para no tener que realizar más peticiones.
	 *
	 * @return Nueva instancia de EESSParser con los datos de referencia cargados.
	 */
	private EESSParser createANewEESSParser() {
		cargarDatosEnCache();
		// Creamos el objeto con los datos solicitados.
		return new EESSParser();
	}

	/**
	 * Carga de datos en la caché de la aplicación para optimizar el parseo.
	 * El objetivo es invocar a los distintos metodos de servicio para que hagan la
	 * consulta a la BD y carguen en caché aquellos datos normalmente "inmutables".
	 * <p>
	 * Confiamos ciegamente en que los servicios hagan bien su trabajo y que no hay
	 * modificaciones de esos datos durante la ejecución del cron.
	 */
	private void cargarDatosEnCache() {
		long tiempoCargaDatosInicialesCacheInicio = System.currentTimeMillis();
		// Obtenemos la instancia de la factoría de persistencia para tener código más limpio
		ServiceFactory serviceFactory = Factorias.service;

		loggerParse.info(ETIQUETA_LOGGER +
								 "Se procede a cargar en caché los datos de referencia necesarios para el parseo de EESS.");
		// Cargar los datos de referencia solicitados para optimizar parseo
		// Estos datos se cargarán en caché para futuros usos
		serviceFactory.forCombustible().findAllCombustibles(); // combustibles
		serviceFactory.forCCAAService().findAllComunidadesAutonomas();     // CCAA
		serviceFactory.forProvincia().findAllProvincias();     // provincias
		serviceFactory.forMunicipio().findAllMunicipios();     // municipios

		this.tiempoCargaDatosInicialesCache =
				System.currentTimeMillis() - tiempoCargaDatosInicialesCacheInicio;
		loggerParse.info(
				ETIQUETA_LOGGER + "Cargados los datos en caché. Tiempo empleado: {} ms.",
				this.tiempoCargaDatosInicialesCache);
	}

	/**
	 * Parsea una EESS concreta del DTO a entidad de dominio.
	 *
	 * @param eessParser            Instancia del parser de EESS ya creada con datos de referencia.
	 * @param estacionDeServicioDTO EESS en formato DTO
	 * @return EESS en formato dominio
	 * @throws IllegalArgumentException Si en el proceso de parseo ocurre algún error.
	 */
	private EstacionDeServicio parseESADominio(EESSParser eessParser,
											   ESParserDTO estacionDeServicioDTO) {
		if (estacionDeServicioDTO == null) throw new IllegalArgumentException(
				"La estación de servicio (EESS) recibida es nula.");

		// Usamos el eessParser definido anteriormente para parsear cada EESS
		try {
			return eessParser.parseEESS(estacionDeServicioDTO, this.fechaDeParser);
		} catch (IllegalArgumentException | IllegalStateException e) {
			loggerParse.error(ETIQUETA_LOGGER +
									  "Error al parsear la estación de servicio (EESS) con código externo {} - Se ignora: {}",
							  estacionDeServicioDTO.getIdeess(), e.getMessage(), e);
			return null;
		}
	}


	/**
	 * Método principal que procesa un JSON completo de EESS.
	 *
	 * @param json JSON recibido de la API con lista de EESS y fecha de precios
	 * @return Lista de EESS parseadas
	 */
	//	public List<ES> parseAll(JSONObject json) { TODO
	//		List<ES> resultado = new ArrayList<>();
	//		List<JSONObject> fallidos = new ArrayList<>();
	//
	//		LocalDate fecha = parseFecha(json);
	//		JSONArray listaEESS = json.optJSONArray(
	//				propertyLoader.getJsonKeyProperty("lista.eess.precio"));
	//		if (listaEESS == null) return resultado;
	//
	//		int totalItems = listaEESS.length();
	//		parseLog.info("<<< Inicio del parseado de EESS >>> Total: {}", totalItems);
	//
	//		// Crear pool de hilos seguro
	//		int numHilos = Math.min(Runtime.getRuntime().availableProcessors(),
	//								Integer.parseInt(propertyLoader.getApplicationProperty(
	//										"parser.max.threads")));
	//
	//		ExecutorService executor = Executors.newFixedThreadPool(numHilos);
	//
	//		//		try {
	//		//			List<CompletableFuture<Void>> futures = new ArrayList<>();
	//		//
	//		//			for (int i = 0; i < totalItems; i++) {
	//		//				final int index = i;
	//		//				final JSONObject item = listaEESS.getJSONObject(i);
	//		//
	//		//				CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
	//		//					try {
	//		//						ES ES = eessParser.parse(item, fecha);
	//		//						if (ES != null) {
	//		//							synchronized (resultado) {
	//		//								resultado.add(ES);
	//		//							}
	//		//						}
	//		//					} catch (Exception e) {
	//		//						synchronized (fallidos) {
	//		//							fallidos.add(item);
	//		//						}
	//		//						parseLog.error("Error al parsear estación json {}: {}",
	//		//									   item.toString(), e.getMessage(), e);
	//		//					}
	//		//				}, executor);
	//		//
	//		//				futures.add(future);
	//		//			}
	//		//
	//		//			// Esperar que todos terminen
	//		//			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
	//		//
	//		//		} finally {
	//		//			// Cerrar correctamente los hilos
	//		//			executor.shutdown();
	//		//			try {
	//		//				if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
	//		//					executor.shutdownNow();
	//		//				}
	//		//			} catch (InterruptedException e) {
	//		//				executor.shutdownNow();
	//		//				Thread.currentThread().interrupt();
	//		//			}
	//		//		}
	//
	//		// Resumen final
	//		int parseados = resultado.size();
	//		int fallidas = fallidos.size();
	//		double porcentajeFallidas = totalItems > 0 ? (fallidas * 100.0 / totalItems) : 0;
	//
	//		StringBuilder resumen = new StringBuilder();
	//		resumen.append("===== RESUMEN Parser EESS =====\n").append("Total EESS en JSON: ")
	//				.append(totalItems).append("\n").append("Parseadas correctamente: ")
	//				.append(parseados).append("\n").append("Fallidas: ").append(fallidas)
	//				.append(" (").append(String.format("%.2f", porcentajeFallidas))
	//				.append("%)\n").append("Número de hilos usados: ").append(numHilos)
	//				.append("\n");
	//
	//		if (!fallidos.isEmpty()) {
	//			resumen.append("JSON de EESS que fallaron al parsear:\n");
	//			for (JSONObject fallo : fallidos) {
	//				resumen.append(fallo.toString()).append("\n");
	//			}
	//		}
	//
	//		resumen.append("===============================\n");
	//		loggerParse.info(resumen.toString());
	//
	//		return resultado;
	//	}


}
