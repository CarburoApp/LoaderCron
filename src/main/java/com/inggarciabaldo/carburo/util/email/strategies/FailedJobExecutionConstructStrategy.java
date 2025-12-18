package com.inggarciabaldo.carburo.util.email.strategies;

import com.inggarciabaldo.carburo.scheduler.jobs.DatosDeEjecucion;
import com.inggarciabaldo.carburo.util.email.EmailContent;
import com.inggarciabaldo.carburo.util.log.Loggers;
import com.inggarciabaldo.carburo.util.properties.PropertyLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Estrategia concreta para correos de ERROR en la ejecución del Job.
 * <p>
 * Construye:
 * - subject de error
 * - cuerpo en texto plano
 * - adjuntos (JSON + log)
 */
public class FailedJobExecutionConstructStrategy implements EmailConstructStrategy {

	/* =======================
	 * Constantes generales
	 * ======================= */

	private static final String SUBJECT_PREFIX = "Carburo - LoaderCron - [ERROR] Error de ejecución en el Cron - ";
	private static final PropertyLoader CONFIG = PropertyLoader.getInstance();
	private static final String EMAIL_TEMPLATE_FILE = "email_template_error.html";


	/* =======================
	 * Estado
	 * ======================= */

	private final DatosDeEjecucion datos;
	private final Throwable exception;

	public FailedJobExecutionConstructStrategy(DatosDeEjecucion datos,
											   Throwable exception) {
		if (datos == null) throw new IllegalArgumentException(
				"Los datos de ejecución no pueden ser nulos.");
		if (exception == null)
			throw new IllegalArgumentException("La excepción no puede ser nula.");
		this.datos     = datos;
		this.exception = exception;
	}

	/* =======================
	 * Strategy
	 * ======================= */

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

	private List<File> buildAttachments() {
		List<File> attachments = new ArrayList<>();
		// JSON (si existe)
		File json = datos.getArchivoRespuestaApiJson();
		if (json != null && json.exists()) attachments.add(json);
		// Log del cron
		File logFile = buildCronLogFile();
		if (logFile.exists()) attachments.add(logFile);
		return attachments;
	}

	/**
	 * Construye el fichero de log asociado a la ejecución del cron.
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
	private String buildEmailHtml() throws IOException {
		try (InputStream input = getClass().getClassLoader()
				.getResourceAsStream(EMAIL_TEMPLATE_FILE)) {
			if (input == null) throw new IllegalStateException(
					"No se encontró el template del correo: " + EMAIL_TEMPLATE_FILE);
			String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

			Map<String, String> placeholders;
			placeholders = Map.of("{{fechaInicio}}",
								  datos.formatoHora(datos.getFechaInicioEjecucion()),
								  "{{fechaFin}}",
								  datos.formatoHora(datos.getFechaFinEjecucion()),
								  "{{duracion}}",
								  datos.formatoTiempo(datos.getTiempoTotalCronMs()),
								  "{{tipoExcepcion}}", exception.getClass().getName(),
								  "{{mensajeExcepcion}}", exception.getMessage(),
								  "{{warningsDetectados}}",
								  buildHtmlList(datos.getWarningsDetectados(), "warning"),
								  "{{erroresDetectados}}",
								  buildHtmlList(datos.getErroresDetectados(), "error"));
			for (Map.Entry<String, String> entry : placeholders.entrySet())
				template = template.replace(entry.getKey(), entry.getValue());
			return template;
		}
	}

	private String buildHtmlList(List<String> items, String cssClass) {
		if (items == null || items.isEmpty())
			return "<li>No hay " + (cssClass.equals("warning") ? "warnings" : "errores") +
					"</li>";
		return items.stream()
				.map(item -> "<li class='" + cssClass + "'>" + item + "</li>")
				.collect(Collectors.joining());
	}
}
