package com.inggarciabaldo.carburo.util.email.strategies;

import com.inggarciabaldo.carburo.scheduler.jobs.DatosDeEjecucion;
import com.inggarciabaldo.carburo.util.email.EmailContent;
import com.inggarciabaldo.carburo.util.log.Loggers;
import com.inggarciabaldo.carburo.util.properties.PropertyLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Estrategia concreta para correos de ejecución correcta del Job.
 * <p>
 * Se encarga de construir:
 * - subject
 * - cuerpo HTML
 * - adjuntos (JSON + log del cron)
 */
public class CorrectJobExecutionConstructStrategy implements EmailConstructStrategy {

	/* =======================
	 * Constantes generales
	 * ======================= */

	private static final String EMAIL_TEMPLATE_PATH = "src/main/resources/email_template_correct_execution.html";
	private static final String SUBJECT_PREFIX = "Carburo - LoaderCron - [Exitoso] Reporte de ejecución - ";

	private static final PropertyLoader CONFIG = PropertyLoader.getInstance();

	private final DatosDeEjecucion datos;

	public CorrectJobExecutionConstructStrategy(DatosDeEjecucion datos) {
		if (datos == null) throw new IllegalArgumentException(
				"Los datos de ejecución no pueden ser nulos.");
		this.datos = datos;
	}

	@Override
	public EmailContent buildEmailContent() {
		try {
			String html = buildEmailHtml();
			String subject = SUBJECT_PREFIX +
					datos.formatoDiaHoraMinuto(datos.getFechaInicioEjecucion());

			return new EmailContent(subject, html, buildAttachments());
		} catch (IOException e) {
			throw new IllegalStateException("Error construyendo el contenido del correo",
											e);
		}
	}

	/* =======================
	 * Adjuntos
	 * ======================= */

	/**
	 * Construye la lista de adjuntos:
	 * - JSON de respuesta (si existe)
	 * - Log del cron asociado a la ejecución
	 */
	private List<File> buildAttachments() {
		List<File> attachments = new ArrayList<>();
		// JSON de respuesta
		File json = datos.getArchivoRespuestaApiJson();
		if (json != null && json.exists()) attachments.add(json);
		// Log del cron
		File logFile = buildCronLogFile();
		if (logFile.exists()) {
			attachments.add(logFile);
		}

		return attachments;
	}

	/**
	 * Construye el fichero de log del cron en base a:
	 * - Directorio de logs
	 * - Nombre del fichero
	 * - Fecha de inicio de ejecución
	 */
	private File buildCronLogFile() {

		String logDir = CONFIG.getApplicationProperty(Loggers.PROP_LOG_DIR,
													  Loggers.DEFAULT_LOG_DIR).trim();

		String timestamp = datos.getFechaInicioEjecucion()
				.format(DateTimeFormatter.ofPattern(
						Loggers.LOG_FILE_CRON_JOB_EXEC_DATETIME_FORMAT));

		String fileName = String.format(Loggers.LOG_FILE_CRON_JOB_EXEC, timestamp);

		return Path.of(logDir, fileName).toFile();
	}

	/* =======================
	 * Construcción HTML
	 * ======================= */

	// Método auxiliar para construir <ul> con clase CSS para warnings/errores
	private String buildHtmlList(List<String> items, String cssClass) {
		if (items == null || items.isEmpty()) return "<li>No hay " + (cssClass.equals("warning") ? "warnings" : "errores") + "</li>";
		return items.stream()
				.map(item -> "<li class='" + cssClass + "'>" + item + "</li>")
				.collect(Collectors.joining());
	}

	private String buildEmailHtml() throws IOException {
		String template = Files.readString(Path.of(EMAIL_TEMPLATE_PATH));

		for (Map.Entry<String, String> entry : buildTemplateValues().entrySet()) {
			template = template.replace(entry.getKey(), entry.getValue());
		}

		return template;
	}

	private Map<String, String> buildTemplateValues() {
		return Map.ofEntries(
				Map.entry(PH_FECHA_INICIO_EJECUCION, datos.formatoHora(datos.getFechaInicioEjecucion())),
				Map.entry(PH_FECHA_FIN_EJECUCION, datos.formatoHora(datos.getFechaFinEjecucion())),
				Map.entry(PH_TIEMPO_TOTAL_CRON_MS, datos.formatoTiempo(datos.getTiempoTotalCronMs())),
				Map.entry(PH_FECHA_DATOS_PROCESADOS, datos.getFechaDeDatosProcesados().toString()),

				Map.entry(PH_API_HTTP_INTENTOS_REALIZADOS, String.valueOf(datos.getApiHttpIntentosRealizados())),
				Map.entry(PH_API_TOTAL_EESS_JSON, String.valueOf(datos.getApiTotalEESSRecibidasJson())),
				Map.entry(PH_API_RESPUESTA_VALIDA, String.valueOf(datos.isApiRespuestaValida())),
				Map.entry(PH_TIEMPO_TOTAL_HTTP_REQUEST_MS, datos.formatoTiempo(datos.getTiempoTotalHttpRequestMs())),

				Map.entry(PH_PARSEO_JSON_A_DTO_TIEMPO_MS, datos.formatoTiempo(datos.getParseoJsonADtoTiempoMs())),
				Map.entry(PH_PARSEO_TOTAL_EESS_EN_DTO, String.valueOf(datos.getParseoTotalEESSEnDto())),

				Map.entry(PH_PARSEO_EESS_TOTAL, String.valueOf(datos.getParseoEESSTotal())),
				Map.entry(PH_PARSEO_EESS_CORRECTAS, String.valueOf(datos.getParseoEESSCorrectas())),
				Map.entry(PH_PARSEO_EESS_ERRONEAS, String.valueOf(datos.getParseoEESSErroneas())),
				Map.entry(PH_PARSEO_PRECIOS_CORRECTOS, String.valueOf(datos.getParseoPreciosCorrectos())),
				Map.entry(PH_TIEMPO_TOTAL_PARSEO_MS, datos.formatoTiempo(datos.getTiempoTotalParseoMs())),

				Map.entry(PH_BD_CARGA_INICIAL_EESS_TIEMPO_MS, datos.formatoTiempo(datos.getBdCargaInicialEESSTiempoMs())),
				Map.entry(PH_BD_TOTAL_EESS_INICIALES, String.valueOf(datos.getBdTotalEESSIniciales())),

				Map.entry(PH_DECISION_EESS_ACTUALIZAR_TIEMPO_MS, datos.formatoTiempo(datos.getDecisionEESSActualizarTiempoMs())),
				Map.entry(PH_DECISION_DISPONIBILIDAD_INSERTAR_TIEMPO_MS, datos.formatoTiempo(datos.getDecisionDisponibilidadInsertarTiempoMs())),
				Map.entry(PH_DECISION_PRECIOS_INSERTAR_TIEMPO_MS, datos.formatoTiempo(datos.getDecisionPreciosInsertarTiempoMs())),
				Map.entry(PH_DECISION_PRECIOS_ACTUALIZAR_TIEMPO_MS, datos.formatoTiempo(datos.getDecisionPreciosActualizarTiempoMs())),
				Map.entry(PH_DECISION_EESS_NUEVAS, String.valueOf(datos.getDecisionEESSNuevas())),
				Map.entry(PH_DECISION_EESS_YA_PRESENTES, String.valueOf(datos.getDecisionEESSYaPresentes())),
				Map.entry(PH_DECISION_EESS_ACTUALIZAR, String.valueOf(datos.getDecisionEESSActualizar())),
				Map.entry(PH_DECISION_PRECIOS_INSERTAR, String.valueOf(datos.getDecisionPreciosInsertar())),
				Map.entry(PH_DECISION_PRECIOS_ACTUALIZAR, String.valueOf(datos.getDecisionPreciosActualizar())),
				Map.entry(PH_DECISION_DISPONIBILIDADES_INSERTAR, String.valueOf(datos.getDecisionDisponibilidadesInsertar())),

				Map.entry(PH_PERSISTENCIA_EESS_INSERT_TIEMPO_MS, datos.formatoTiempo(datos.getPersistenciaEESSInsertTiempoMs())),
				Map.entry(PH_PERSISTENCIA_EESS_INSERTADAS, String.valueOf(datos.getPersistenciaEESSInsertadas())),
				Map.entry(PH_PERSISTENCIA_EESS_UPDATE_TIEMPO_MS, datos.formatoTiempo(datos.getPersistenciaEESSUpdateTiempoMs())),
				Map.entry(PH_PERSISTENCIA_EESS_ACTUALIZADAS, String.valueOf(datos.getPersistenciaEESSActualizadas())),

				Map.entry(PH_PERSISTENCIA_PRECIOS_INSERT_TIEMPO_MS, datos.formatoTiempo(datos.getPersistenciaPreciosInsertTiempoMs())),
				Map.entry(PH_PERSISTENCIA_PRECIOS_INSERTADOS, String.valueOf(datos.getPersistenciaPreciosInsertados())),
				Map.entry(PH_PERSISTENCIA_PRECIOS_UPDATE_TIEMPO_MS, datos.formatoTiempo(datos.getPersistenciaPreciosUpdateTiempoMs())),
				Map.entry(PH_PERSISTENCIA_PRECIOS_ACTUALIZADOS, String.valueOf(datos.getPersistenciaPreciosActualizados())),

				Map.entry(PH_PERSISTENCIA_DISPONIBILIDADES_INSERT_TIEMPO_MS, datos.formatoTiempo(datos.getPersistenciaDisponibilidadesInsertTiempoMs())),
				Map.entry(PH_PERSISTENCIA_DISPONIBILIDADES_INSERTADAS, String.valueOf(datos.getPersistenciaDisponibilidadesInsertadas())),

				Map.entry(PH_WARNINGS, buildHtmlList(datos.getWarningsDetectados(), "warning")),
				Map.entry(PH_ERRORS, buildHtmlList(datos.getErroresDetectados(), "error"))
							);
	}


	/* Placeholders para la plantilla HTML */
	private static final String PH_FECHA_INICIO_EJECUCION = "{{fechaInicioEjecucion}}";
	private static final String PH_FECHA_FIN_EJECUCION = "{{fechaFinEjecucion}}";
	private static final String PH_TIEMPO_TOTAL_CRON_MS = "{{tiempoTotalCronMs}}";
	private static final String PH_FECHA_DATOS_PROCESADOS = "{{fechaDeDatosProcesados}}";

	private static final String PH_API_HTTP_INTENTOS_REALIZADOS = "{{apiHttpIntentosRealizados}}";
	private static final String PH_API_TOTAL_EESS_JSON = "{{apiTotalEESSRecibidasJson}}";
	private static final String PH_API_RESPUESTA_VALIDA = "{{apiRespuestaValida}}";
	private static final String PH_TIEMPO_TOTAL_HTTP_REQUEST_MS = "{{tiempoTotalHttpRequestMs}}";

	private static final String PH_PARSEO_JSON_A_DTO_TIEMPO_MS = "{{parseoJsonADtoTiempoMs}}";
	private static final String PH_PARSEO_TOTAL_EESS_EN_DTO = "{{parseoTotalEESSEnDto}}";

	private static final String PH_PARSEO_EESS_TOTAL = "{{parseoEESSTotal}}";
	private static final String PH_PARSEO_EESS_CORRECTAS = "{{parseoEESSCorrectas}}";
	private static final String PH_PARSEO_EESS_ERRONEAS = "{{parseoEESSErroneas}}";
	private static final String PH_PARSEO_PRECIOS_CORRECTOS = "{{parseoPreciosCorrectos}}";
	private static final String PH_TIEMPO_TOTAL_PARSEO_MS = "{{tiempoTotalParseoMs}}";

	private static final String PH_BD_CARGA_INICIAL_EESS_TIEMPO_MS = "{{bdCargaInicialEESSTiempoMs}}";
	private static final String PH_BD_TOTAL_EESS_INICIALES = "{{bdTotalEESSIniciales}}";

	private static final String PH_DECISION_EESS_ACTUALIZAR_TIEMPO_MS = "{{decisionEESSActualizarTiempoMs}}";
	private static final String PH_DECISION_DISPONIBILIDAD_INSERTAR_TIEMPO_MS = "{{decisionDisponibilidadInsertarTiempoMs}}";
	private static final String PH_DECISION_PRECIOS_INSERTAR_TIEMPO_MS = "{{decisionPreciosInsertarTiempoMs}}";
	private static final String PH_DECISION_PRECIOS_ACTUALIZAR_TIEMPO_MS = "{{decisionPreciosActualizarTiempoMs}}";
	private static final String PH_DECISION_EESS_NUEVAS = "{{decisionEESSNuevas}}";
	private static final String PH_DECISION_EESS_YA_PRESENTES = "{{decisionEESSYaPresentes}}";
	private static final String PH_DECISION_EESS_ACTUALIZAR = "{{decisionEESSActualizar}}";
	private static final String PH_DECISION_PRECIOS_INSERTAR = "{{decisionPreciosInsertar}}";
	private static final String PH_DECISION_PRECIOS_ACTUALIZAR = "{{decisionPreciosActualizar}}";
	private static final String PH_DECISION_DISPONIBILIDADES_INSERTAR = "{{decisionDisponibilidadesInsertar}}";

	private static final String PH_PERSISTENCIA_EESS_INSERT_TIEMPO_MS = "{{persistenciaEESSInsertTiempoMs}}";
	private static final String PH_PERSISTENCIA_EESS_INSERTADAS = "{{persistenciaEESSInsertadas}}";
	private static final String PH_PERSISTENCIA_EESS_UPDATE_TIEMPO_MS = "{{persistenciaEESSUpdateTiempoMs}}";
	private static final String PH_PERSISTENCIA_EESS_ACTUALIZADAS = "{{persistenciaEESSActualizadas}}";

	private static final String PH_PERSISTENCIA_PRECIOS_INSERT_TIEMPO_MS = "{{persistenciaPreciosInsertTiempoMs}}";
	private static final String PH_PERSISTENCIA_PRECIOS_INSERTADOS = "{{persistenciaPreciosInsertados}}";
	private static final String PH_PERSISTENCIA_PRECIOS_UPDATE_TIEMPO_MS = "{{persistenciaPreciosUpdateTiempoMs}}";
	private static final String PH_PERSISTENCIA_PRECIOS_ACTUALIZADOS = "{{persistenciaPreciosActualizados}}";

	private static final String PH_PERSISTENCIA_DISPONIBILIDADES_INSERT_TIEMPO_MS = "{{persistenciaDisponibilidadesInsertTiempoMs}}";
	private static final String PH_PERSISTENCIA_DISPONIBILIDADES_INSERTADAS = "{{persistenciaDisponibilidadesInsertadas}}";

	private static final String PH_WARNINGS = "{{warningsDetectados}}";
	private static final String PH_ERRORS = "{{erroresDetectados}}";

}
