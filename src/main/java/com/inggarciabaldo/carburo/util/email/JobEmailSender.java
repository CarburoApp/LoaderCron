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
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Clase encargada de registrar métricas y enviar un correo resumen
 * del resultado de un job de sincronización de datos.
 */
public class JobEmailSender {

	private static final PropertyLoader config = PropertyLoader.getInstance();

	private final String fromEmail = config.getApplicationProperty("mail.from");
	private final String password = config.getApplicationProperty("mail.password");
	private final String toEmail = config.getApplicationProperty("mail.to");
	private final String smtpHost = config.getApplicationProperty("mail.smtp.host");
	private final int smtpPort = Integer.parseInt(
			config.getApplicationProperty("mail.smtp.port"));

	private final DatoDeEjecucion dato;

	public JobEmailSender(DatoDeEjecucion dato) {
		this.dato = dato;
	}

	public void sendReport() throws MessagingException, IOException {
		Loggers.CRON.info("Enviando Reporte por correo");

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", smtpHost);
		props.put("mail.smtp.port", String.valueOf(smtpPort));
		props.put("mail.smtp.ssl.trust", smtpHost);

		Loggers.CRON.info("Mail provider: {}",
						  Transport.class.getProtectionDomain().getCodeSource());
		Loggers.CRON.info("Activation provider: {}",
						  DataHandler.class.getProtectionDomain().getCodeSource());

		Session session = Session.getInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(fromEmail, password);
			}
		});

		// Crear mensaje simple
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(fromEmail));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
		message.setSubject("Reporte de ejecución de Carburo - LoaderCron - " +
								   dato.formatoDiaHoraMinuto(
										   dato.getFechaInicioEjecucion()));

		Multipart multipart = new MimeMultipart("mixed");

		// Parte HTML
		MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setContent(buildEmailHtml("src/main/resources/email_template.html"),
							"text/html; charset=utf-8");
		multipart.addBodyPart(htmlPart);

		// Adjuntar JSON si existe
		File jsonFile = dato.getJsonRespuestaArchivo();
		if (jsonFile != null && jsonFile.exists()) {
			MimeBodyPart attachment = new MimeBodyPart();
			DataSource source = new FileDataSource(jsonFile);
			attachment.setDataHandler(new DataHandler(source));
			attachment.setFileName(jsonFile.getName());
			multipart.addBodyPart(attachment);
		}

		message.setContent(multipart);
		Transport.send(message);
	}

	private String buildEmailHtml(String templatePath) throws IOException {
		String template = new String(Files.readAllBytes(Paths.get(templatePath)));

		template = template.replace("{{START_TIME}}",
									dato.formatoHora(dato.getFechaInicioEjecucion()))
				.replace("{{END_TIME}}", dato.formatoHora(dato.getFechaFinEjecucion()))
				.replace("{{TOTAL_DURATION}}",
						 dato.formatoTiempo(dato.getTiempoTotalCron()))
				.replace("{{DB_STATUS}}", "Correcto") // se puede mejorar con info real
				.replace("{{API_STATUS}}", "Correcto")
				.replace("{{DB_TIME}}", String.valueOf(dato.getTiempoObtenerEESSBD()))
				.replace("{{API_TIME}}", String.valueOf(dato.getTiempoPeticionApiMs()))
				.replace("{{TIME_JSON_PARSE}}",
						 String.valueOf(dato.getTiempoJSONParseoDTOMs()))
				.replace("{{TIME_DTO_PARSE}}",
						 String.valueOf(dato.getTiempoDTOParseoEntidades()))
				.replace("{{TIME_PARSE_EESS}}",
						 String.valueOf(dato.getTiempoParseoEESSMs()))
				.replace("{{TIME_PERSISTENCIA}}",
						 String.valueOf(dato.getTiempoPersistenciaMs()))
				.replace("{{TOTAL_EESS_JSON}}", String.valueOf(dato.getTotalEESSEnJson()))
				.replace("{{TOTAL_EESS_DTO}}", String.valueOf(dato.getTotalEESSEnDTO()))
				.replace("{{TOTAL_EESS_PARSED}}",
						 String.valueOf(dato.getTotalEESSParseadas()))
				.replace("{{PCT_EESS_PARSED}}",
						 String.format("%.2f", dato.getPorcentajeParseoEESS()))
				.replace("{{TOTAL_EESS_PARSE_ERRORS}}",
						 String.valueOf(dato.getTotalEESSNoParseadasConErrores()))
				.replace("{{TOTAL_EESS_PARSED_OUTSIDE_DB}}",
						 String.valueOf(dato.getTotalEESSParseadasFueraDeBD()))
				.replace("{{TOTAL_EESS_PARSED_IN_DB}}",
						 String.valueOf(dato.getTotalEESSParseadasEnBD()))
				.replace("{{TOTAL_EESS_TO_UPDATE}}", String.valueOf(
						dato.getTotalEESSParseadasRequierenActualizacion()))
				.replace("{{TOTAL_EESS_INSERTED}}",
						 String.valueOf(dato.getTotalEESSInsertadas()))
				.replace("{{TOTAL_EESS_UPDATED}}",
						 String.valueOf(dato.getTotalEESSActualizadas()))
				.replace("{{TOTAL_EESS_PERSISTED}}", String.valueOf(
						dato.getTotalEESSInsertadas() + dato.getTotalEESSActualizadas()))
				.replace("{{TOTAL_EESS_PRICE_OUTSIDE_DB}}", String.valueOf(
						dato.getTotalEESSPrecioYDisponibilidadFueraDeBDInsertadas()))
				.replace("{{TOTAL_PRICES_TO_INSERT}}",
						 String.valueOf(dato.getTotalProcesamientoPreciosAInsertar()))
				.replace("{{TOTAL_PRICES_INSERTED}}",
						 String.valueOf(dato.getTotalPreciosInsertados()))
				.replace("{{TOTAL_PRICES_TO_UPDATE}}",
						 String.valueOf(dato.getTotalProcesamientoPreciosAActualizar()))
				.replace("{{TOTAL_PRICES_UPDATED}}",
						 String.valueOf(dato.getTotalPreciosActualizados()))
				.replace("{{TOTAL_PRICES_PERSISTED}}", String.valueOf(
						dato.getTotalPreciosInsertados() +
								dato.getTotalPreciosActualizados()));

		// Warnings y errores (si los hubiere)
		String warningsHtml = ""; // si tienes lista de warnings, meter aquí
		String errorsHtml = "";   // si tienes lista de errores, meter aquí
		template = template.replace("{{WARNINGS}}", warningsHtml)
				.replace("{{ERRORS}}", errorsHtml);

		return template;
	}
}