package com.inggarciabaldo.carburo.util.email;

import com.inggarciabaldo.carburo.scheduler.jobs.DatoDeEjecucion;
import com.inggarciabaldo.carburo.util.log.Loggers;
import com.inggarciabaldo.carburo.util.properties.PropertyLoader;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/**
 * Responsable de construir y enviar el correo resumen
 * de la ejecución de un Job Quartz.
 */
public class JobEmailSender {

	/* =======================
	 * Constantes generales
	 * ======================= */

	private static final String EMAIL_TEMPLATE_PATH = "src/main/resources/email_template.html";

	private static final String MULTIPART_MIXED = "mixed";
	private static final String CONTENT_TYPE_HTML_UTF8 = "text/html; charset=utf-8";

	private static final String SUBJECT_PREFIX = "Reporte de ejecución de Carburo - LoaderCron - ";

	private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
	private static final String MAIL_SMTP_STARTTLS = "mail.smtp.starttls.enable";
	private static final String MAIL_SMTP_HOST = "mail.smtp.host";
	private static final String MAIL_SMTP_PORT = "mail.smtp.port";
	private static final String MAIL_SMTP_SSL_TRUST = "mail.smtp.ssl.trust";

	private static final String VALUE_TRUE = "true";
	private static final String STATUS_OK = "Correcto";

	/* =======================
	 * Configuración mail
	 * ======================= */

	private static final PropertyLoader CONFIG = PropertyLoader.getInstance();

	private final String fromEmail = CONFIG.getApplicationProperty("mail.from");
	private final String password = CONFIG.getApplicationProperty("mail.password");
	private final String toEmail = CONFIG.getApplicationProperty("mail.to");
	private final String smtpHost = CONFIG.getApplicationProperty("mail.smtp.host");
	private final int smtpPort = Integer.parseInt(
			CONFIG.getApplicationProperty("mail.smtp.port"));

	private final DatoDeEjecucion dato;

	public JobEmailSender(DatoDeEjecucion dato) {
		this.dato = dato;
	}

	/* =======================
	 * API pública
	 * ======================= */

	/**
	 * Construye y envía el correo con el informe de ejecución.
	 */
	public void sendReport() throws MessagingException, IOException {
		Loggers.CRON.info("Enviando Reporte por correo");

		Session session = createMailSession();
		Message message = createBaseMessage(session);

		Multipart multipart = new MimeMultipart(MULTIPART_MIXED);
		multipart.addBodyPart(buildHtmlBodyPart());
		addJsonAttachmentIfExists(multipart);

		message.setContent(multipart);
		Transport.send(message);
	}

	/* =======================
	 * Construcción sesión
	 * ======================= */

	private Session createMailSession() {
		Properties props = new Properties();
		props.put(MAIL_SMTP_AUTH, VALUE_TRUE);
		props.put(MAIL_SMTP_STARTTLS, VALUE_TRUE);
		props.put(MAIL_SMTP_HOST, smtpHost);
		props.put(MAIL_SMTP_PORT, String.valueOf(smtpPort));
		props.put(MAIL_SMTP_SSL_TRUST, smtpHost);

		return Session.getInstance(props, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(fromEmail, password);
			}
		});
	}


	/* =======================
	 * Construcción mensaje
	 * ======================= */

	private Message createBaseMessage(Session session) throws MessagingException {
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(fromEmail));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
		message.setSubject(SUBJECT_PREFIX + dato.formatoDiaHoraMinuto(
				dato.getFechaInicioEjecucion()));
		return message;
	}

	private MimeBodyPart buildHtmlBodyPart() throws MessagingException, IOException {
		MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setContent(buildEmailHtml(), CONTENT_TYPE_HTML_UTF8);
		return htmlPart;
	}

	private void addJsonAttachmentIfExists(Multipart multipart) throws MessagingException {

		File jsonFile = dato.getJsonRespuestaArchivo();
		if (jsonFile == null || !jsonFile.exists()) {
			return;
		}

		MimeBodyPart attachment = new MimeBodyPart();
		DataSource source = new FileDataSource(jsonFile);
		attachment.setDataHandler(new DataHandler(source));
		attachment.setFileName(jsonFile.getName());
		multipart.addBodyPart(attachment);
	}

	/* =======================
	 * Template HTML
	 * ======================= */

	private String buildEmailHtml() throws IOException {
		String template = Files.readString(Path.of(EMAIL_TEMPLATE_PATH));

		for (Map.Entry<String, String> entry : buildTemplateValues().entrySet()) {
			template = template.replace(entry.getKey(), entry.getValue());
		}

		return template;
	}

	private Map<String, String> buildTemplateValues() {
		return Map.ofEntries(Map.entry(PH_START_TIME,
									   dato.formatoHora(dato.getFechaInicioEjecucion())),
							 Map.entry(PH_END_TIME,
									   dato.formatoHora(dato.getFechaFinEjecucion())),
							 Map.entry(PH_TOTAL_DURATION,
									   dato.formatoTiempo(dato.getTiempoTotalCron())),
							 Map.entry(PH_DB_STATUS, STATUS_OK),
							 Map.entry(PH_API_STATUS, STATUS_OK), Map.entry(PH_DB_TIME,
																			String.valueOf(
																					dato.getTiempoObtenerEESSBD())),
							 Map.entry(PH_API_TIME,
									   String.valueOf(dato.getTiempoPeticionApiMs())),
							 Map.entry(PH_TIME_JSON_PARSE,
									   String.valueOf(dato.getTiempoJSONParseoDTOMs())),
							 Map.entry(PH_TIME_DTO_PARSE, String.valueOf(
									 dato.getTiempoDTOParseoEntidades())),
							 Map.entry(PH_TIME_PARSE_EESS,
									   String.valueOf(dato.getTiempoParseoEESSMs())),
							 Map.entry(PH_TIME_PERSISTENCIA,
									   String.valueOf(dato.getTiempoPersistenciaMs())),
							 Map.entry(PH_TOTAL_EESS_JSON,
									   String.valueOf(dato.getTotalEESSEnJson())),
							 Map.entry(PH_TOTAL_EESS_DTO,
									   String.valueOf(dato.getTotalEESSEnDTO())),
							 Map.entry(PH_TOTAL_EESS_PARSED,
									   String.valueOf(dato.getTotalEESSParseadas())),
							 Map.entry(PH_PCT_EESS_PARSED, String.format("%.2f",
																		 dato.getPorcentajeParseoEESS())),
							 Map.entry(PH_TOTAL_EESS_PARSE_ERRORS, String.valueOf(
									 dato.getTotalEESSNoParseadasConErrores())),
							 Map.entry(PH_TOTAL_EESS_PARSED_OUTSIDE_DB, String.valueOf(
									 dato.getTotalEESSParseadasFueraDeBD())),
							 Map.entry(PH_TOTAL_EESS_PARSED_IN_DB,
									   String.valueOf(dato.getTotalEESSParseadasEnBD())),
							 Map.entry(PH_TOTAL_EESS_TO_UPDATE, String.valueOf(
									 dato.getTotalEESSParseadasRequierenActualizacion())),
							 Map.entry(PH_TOTAL_EESS_INSERTED,
									   String.valueOf(dato.getTotalEESSInsertadas())),
							 Map.entry(PH_TOTAL_EESS_UPDATED,
									   String.valueOf(dato.getTotalEESSActualizadas())),
							 Map.entry(PH_TOTAL_EESS_PERSISTED, String.valueOf(
									 dato.getTotalEESSInsertadas() +
											 dato.getTotalEESSActualizadas())),
							 Map.entry(PH_TOTAL_EESS_PRICE_OUTSIDE_DB, String.valueOf(
									 dato.getTotalEESSPrecioYDisponibilidadFueraDeBDInsertadas())),
							 Map.entry(PH_TOTAL_PRICES_TO_INSERT, String.valueOf(
									 dato.getTotalProcesamientoPreciosAInsertar())),
							 Map.entry(PH_TOTAL_PRICES_INSERTED,
									   String.valueOf(dato.getTotalPreciosInsertados())),
							 Map.entry(PH_TOTAL_PRICES_TO_UPDATE, String.valueOf(
									 dato.getTotalProcesamientoPreciosAActualizar())),
							 Map.entry(PH_TOTAL_PRICES_UPDATED, String.valueOf(
									 dato.getTotalPreciosActualizados())),
							 Map.entry(PH_TOTAL_PRICES_PERSISTED, String.valueOf(
									 dato.getTotalPreciosInsertados() +
											 dato.getTotalPreciosActualizados())),
							 Map.entry(PH_WARNINGS, ""), Map.entry(PH_ERRORS, ""));
	}

	/* =======================
	 * Placeholders template
	 * ======================= */

	private static final String PH_START_TIME = "{{START_TIME}}";
	private static final String PH_END_TIME = "{{END_TIME}}";
	private static final String PH_TOTAL_DURATION = "{{TOTAL_DURATION}}";
	private static final String PH_DB_STATUS = "{{DB_STATUS}}";
	private static final String PH_API_STATUS = "{{API_STATUS}}";
	private static final String PH_DB_TIME = "{{DB_TIME}}";
	private static final String PH_API_TIME = "{{API_TIME}}";
	private static final String PH_TIME_JSON_PARSE = "{{TIME_JSON_PARSE}}";
	private static final String PH_TIME_DTO_PARSE = "{{TIME_DTO_PARSE}}";
	private static final String PH_TIME_PARSE_EESS = "{{TIME_PARSE_EESS}}";
	private static final String PH_TIME_PERSISTENCIA = "{{TIME_PERSISTENCIA}}";
	private static final String PH_TOTAL_EESS_JSON = "{{TOTAL_EESS_JSON}}";
	private static final String PH_TOTAL_EESS_DTO = "{{TOTAL_EESS_DTO}}";
	private static final String PH_TOTAL_EESS_PARSED = "{{TOTAL_EESS_PARSED}}";
	private static final String PH_PCT_EESS_PARSED = "{{PCT_EESS_PARSED}}";
	private static final String PH_TOTAL_EESS_PARSE_ERRORS = "{{TOTAL_EESS_PARSE_ERRORS}}";
	private static final String PH_TOTAL_EESS_PARSED_OUTSIDE_DB = "{{TOTAL_EESS_PARSED_OUTSIDE_DB}}";
	private static final String PH_TOTAL_EESS_PARSED_IN_DB = "{{TOTAL_EESS_PARSED_IN_DB}}";
	private static final String PH_TOTAL_EESS_TO_UPDATE = "{{TOTAL_EESS_TO_UPDATE}}";
	private static final String PH_TOTAL_EESS_INSERTED = "{{TOTAL_EESS_INSERTED}}";
	private static final String PH_TOTAL_EESS_UPDATED = "{{TOTAL_EESS_UPDATED}}";
	private static final String PH_TOTAL_EESS_PERSISTED = "{{TOTAL_EESS_PERSISTED}}";
	private static final String PH_TOTAL_EESS_PRICE_OUTSIDE_DB = "{{TOTAL_EESS_PRICE_OUTSIDE_DB}}";
	private static final String PH_TOTAL_PRICES_TO_INSERT = "{{TOTAL_PRICES_TO_INSERT}}";
	private static final String PH_TOTAL_PRICES_INSERTED = "{{TOTAL_PRICES_INSERTED}}";
	private static final String PH_TOTAL_PRICES_TO_UPDATE = "{{TOTAL_PRICES_TO_UPDATE}}";
	private static final String PH_TOTAL_PRICES_UPDATED = "{{TOTAL_PRICES_UPDATED}}";
	private static final String PH_TOTAL_PRICES_PERSISTED = "{{TOTAL_PRICES_PERSISTED}}";
	private static final String PH_WARNINGS = "{{WARNINGS}}";
	private static final String PH_ERRORS = "{{ERRORS}}";

}
