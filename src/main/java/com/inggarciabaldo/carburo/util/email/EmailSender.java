package com.inggarciabaldo.carburo.util.email;

import com.inggarciabaldo.carburo.util.email.strategies.EmailConstructStrategy;
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
import java.util.Properties;

/**
 * Context del patrón Strategy.
 *<p>
 * Responsable de crear la sesión SMTP y enviar el correo.
 */
public class EmailSender {

	/* =======================
	 * Constantes SMTP
	 * ======================= */

	private static final String MULTIPART_MIXED = "mixed";
	private static final String CONTENT_TYPE_HTML_UTF8 = "text/html; charset=utf-8";

	private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
	private static final String MAIL_SMTP_STARTTLS = "mail.smtp.starttls.enable";
	private static final String MAIL_SMTP_HOST = "mail.smtp.host";
	private static final String MAIL_SMTP_PORT = "mail.smtp.port";
	private static final String MAIL_SMTP_SSL_TRUST = "mail.smtp.ssl.trust";

	private static final String VALUE_TRUE = "true";

	/* =======================
	 * Configuración
	 * ======================= */

	private static final PropertyLoader CONFIG = PropertyLoader.getInstance();

	private final String fromEmail = CONFIG.getApplicationProperty("mail.from");
	private final String password = CONFIG.getApplicationProperty("mail.password");
	private final String toEmail = CONFIG.getApplicationProperty("mail.to");
	private final String smtpHost = CONFIG.getApplicationProperty("mail.smtp.host");
	private final int smtpPort = Integer.parseInt(
			CONFIG.getApplicationProperty("mail.smtp.port"));


	// Email que se va a enviar, definido mediante el patrón Strategy
	private final EmailConstructStrategy strategy;

	public EmailSender(EmailConstructStrategy strategy) {
		if (strategy == null) throw new IllegalArgumentException(
				"La estrategia de construcción del correo no puede ser nula.");

		this.strategy = strategy;
	}

	/* =======================
	 * API pública
	 * ======================= */

	/* =======================
	 * API pública
	 * ======================= */

	public void sendEmail() throws MessagingException {
		Loggers.CRON.info("Enviando correo de reporte");

		EmailContent content = strategy.buildEmailContent();

		Session session = createMailSession();
		Message message = createBaseMessage(session, content.subject());

		Multipart multipart = new MimeMultipart(MULTIPART_MIXED);
		multipart.addBodyPart(buildHtmlBodyPart(content.htmlBody()));
		addAttachments(multipart, content);

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

	private Message createBaseMessage(Session session, String subject)
			throws MessagingException {
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(fromEmail));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
		message.setSubject(subject);
		return message;
	}

	private MimeBodyPart buildHtmlBodyPart(String html) throws MessagingException {
		MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setContent(html, CONTENT_TYPE_HTML_UTF8);
		return htmlPart;
	}

	private void addAttachments(Multipart multipart, EmailContent content)
			throws MessagingException {

		for (File file : content.attachments()) {
			if (file == null || !file.exists()) {
				continue;
			}

			MimeBodyPart attachment = new MimeBodyPart();
			DataSource source = new FileDataSource(file);
			attachment.setDataHandler(new DataHandler(source));
			attachment.setFileName(file.getName());
			multipart.addBodyPart(attachment);
		}
	}
}
