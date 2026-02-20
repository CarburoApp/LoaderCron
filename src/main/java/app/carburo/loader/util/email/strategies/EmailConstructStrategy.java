package app.carburo.loader.util.email.strategies;

import app.carburo.loader.util.email.EmailContent;

/**
 * Interfaz Strategy del patrón Strategy.
 * <p>
 * Se encarga EXCLUSIVAMENTE de construir el contenido
 * del correo (subject, body, adjuntos).
 */
public interface EmailConstructStrategy {

	/**
	 * Construye el contenido completo del correo.
	 *
	 * @return EmailContent con toda la información necesaria
	 */
	EmailContent buildEmailContent();
}