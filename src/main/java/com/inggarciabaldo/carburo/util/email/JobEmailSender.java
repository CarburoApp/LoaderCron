package com.inggarciabaldo.carburo.util.email;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Clase encargada de registrar métricas y enviar un correo resumen
 * del resultado de un job de sincronización de datos.
 */
public class JobEmailSender {

	// -------------------------
	// Configuración de correo
	// -------------------------
	private final String fromEmail;
	private final String password; // contraseña de la cuenta remitente
	private final String toEmail; // correo de administración
	private final String smtpHost;
	private final int smtpPort;

	// -------------------------
	// Datos de la ejecución
	// -------------------------
	private LocalDateTime startTime;
	private String dbConnectionStatus;
	private String apiStatus;

	private long dbConnectionTimeMs;
	private long apiConnectionTimeMs;
	private long parseTimeMs;
	private long compareSaveTimeMs;
	private long totalTimeMs;

	private int totalApiElements;
	private int parsedElements;
	private int elementsWithErrors;
	private int newElementsRegistered;
	private int elementsUpdatedManually;
	private int elementsWithPriceErrors;
	private int savedElements;
	private int savedPrices;

	private File apiResponseJsonFile; // adjunto

	private final List<String> warnings = new ArrayList<>();
	private final List<String> errors = new ArrayList<>();

	// -------------------------
	// Constructor
	// -------------------------
	public JobEmailSender(String fromEmail, String password, String toEmail,
						  String smtpHost, int smtpPort) {
		this.fromEmail = fromEmail;
		this.password  = password;
		this.toEmail   = toEmail;
		this.smtpHost  = smtpHost;
		this.smtpPort  = smtpPort;
	}

	// -------------------------
	// Métodos para registrar datos
	// -------------------------

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public void setDbConnectionStatus(String status, long timeMs) {
		this.dbConnectionStatus = status;
		this.dbConnectionTimeMs = timeMs;
		if (!"Correcto".equalsIgnoreCase(status)) {
			errors.add("Error en conexión con la BD");
		}
	}

	public void setApiStatus(String status, long timeMs) {
		this.apiStatus           = status;
		this.apiConnectionTimeMs = timeMs;
		if (!"Correcto".equalsIgnoreCase(status)) {
			errors.add("Error en conexión con la API");
		}
	}

	public void setParseMetrics(int totalApiElements, int parsedElements,
								int elementsWithErrors, int newElementsRegistered,
								int elementsUpdatedManually, long parseTimeMs) {
		this.totalApiElements        = totalApiElements;
		this.parsedElements          = parsedElements;
		this.elementsWithErrors      = elementsWithErrors;
		this.newElementsRegistered   = newElementsRegistered;
		this.elementsUpdatedManually = elementsUpdatedManually;
		this.parseTimeMs             = parseTimeMs;
	}

	public void setSavedMetrics(int savedElements, int savedPrices,
								long compareSaveTimeMs) {
		this.savedElements     = savedElements;
		this.savedPrices       = savedPrices;
		this.compareSaveTimeMs = compareSaveTimeMs;
	}

	public void setApiResponseJsonFile(File file) {
		this.apiResponseJsonFile = file;
	}

	public void addWarning(String warning) {
		warnings.add(warning);
	}

	public void addError(String error) {
		errors.add(error);
	}

	public void setTotalTime(long totalTimeMs) {
		this.totalTimeMs = totalTimeMs;
	}

	// -------------------------
	// Método principal: enviar correo
	// -------------------------
	public void sendReport() throws MessagingException, IOException {
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", smtpHost);
		props.put("mail.smtp.port", String.valueOf(smtpPort));

		Session session = Session.getInstance(props, new Authenticator() {
			protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(fromEmail, password);
			}
		});

		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(fromEmail));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
		message.setSubject("Reporte de ejecución del Job - " + LocalDateTime.now());

		// Contenido HTML

		// Contenido multiparte (HTML + adjunto)
		Multipart multipart = new MimeMultipart();

		// Parte HTML
		MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setContent(buildEmailHtml(
									"src/main/java/com/inggarciabaldo/carburo/util/email/email_template.html"),
							"text/html; charset=utf-8");
		multipart.addBodyPart(htmlPart);

		// Adjuntar JSON si existe
		if (apiResponseJsonFile != null && apiResponseJsonFile.exists()) {
			MimeBodyPart attachmentPart = new MimeBodyPart();
			DataSource source = new FileDataSource(apiResponseJsonFile);
			attachmentPart.setDataHandler(new DataHandler(source));
			attachmentPart.setFileName(apiResponseJsonFile.getName());
			multipart.addBodyPart(attachmentPart);
		}

		message.setContent(multipart);

		Transport.send(message);
		System.out.println("Correo enviado correctamente a " + toEmail);
	}

	public String buildEmailHtml(String templatePath) throws IOException {
		// Leer todo el HTML de la plantilla
		String template = new String(Files.readAllBytes(Paths.get(templatePath)));

		// Reemplazar placeholders por los valores actuales
		template = template.replace("{{START_TIME}}", startTime.toString())
				.replace("{{DB_STATUS}}", dbConnectionStatus)
				.replace("{{API_STATUS}}", apiStatus)
				.replace("{{DB_TIME}}", String.valueOf(dbConnectionTimeMs))
				.replace("{{API_TIME}}", String.valueOf(apiConnectionTimeMs))
				.replace("{{PARSE_TIME}}", String.valueOf(parseTimeMs))
				.replace("{{COMPARE_SAVE_TIME}}", String.valueOf(compareSaveTimeMs))
				.replace("{{TOTAL_TIME}}", String.valueOf(totalTimeMs))
				.replace("{{TOTAL_ELEMENTS}}", String.valueOf(totalApiElements))
				.replace("{{PARSED_ELEMENTS}}", String.valueOf(parsedElements))
				.replace("{{ERROR_ELEMENTS}}", String.valueOf(elementsWithErrors))
				.replace("{{NEW_ELEMENTS}}", String.valueOf(newElementsRegistered))
				.replace("{{MANUAL_UPDATE_ELEMENTS}}",
						 String.valueOf(elementsUpdatedManually))
				.replace("{{SAVED_ELEMENTS}}", String.valueOf(savedElements))
				.replace("{{SAVED_PRICES}}", String.valueOf(savedPrices));

		// Construir warnings y errores en HTML
		String warningsHtml = warnings.stream().map(w -> "<li>" + w + "</li>")
				.collect(Collectors.joining());
		String errorsHtml = errors.stream()
				.map(e -> "<li style='color:red;'>" + e + "</li>")
				.collect(Collectors.joining());

		template = template.replace("{{WARNINGS}}", warningsHtml)
				.replace("{{ERRORS}}", errorsHtml);

		return template;
	}

	// -------------------------
	// Método auxiliar: crear archivo JSON temporal desde String
	// -------------------------
	public static File createTempJsonFile(String jsonContent, String prefix)
			throws IOException {
		File tempFile = File.createTempFile(prefix, ".json");
		try (FileWriter writer = new FileWriter(tempFile)) {
			writer.write(jsonContent);
		}
		return tempFile;
	}
}
