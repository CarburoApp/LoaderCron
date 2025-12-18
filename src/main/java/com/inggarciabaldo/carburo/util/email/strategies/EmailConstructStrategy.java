package com.inggarciabaldo.carburo.util.email.strategies;

import com.inggarciabaldo.carburo.util.email.EmailContent;

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