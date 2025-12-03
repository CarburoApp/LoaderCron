package com.inggarciabaldo.carburo.util.properties;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Clase singleton para manejar propiedades de la aplicación.
 * <p>
 * Permite cargar múltiples archivos de propiedades (application, endpoints, jsonKeys) y acceder
 * a sus valores de manera centralizada. Garantiza que solo exista una instancia de la clase
 * durante toda la ejecución de la aplicación.
 */
public final class PropertyLoader {

	// Propiedades de configuración general de la aplicación
	private final Properties applicationProps = new Properties();

	// Propiedades relacionadas con endpoints de servicios
	private final Properties endpointsProps = new Properties();

	// Propiedades relacionadas con claves JSON o mapeos de datos
	private final Properties jsonKeysProps = new Properties();

	// Propiedades relacionadas con queries JDBC
	private final Properties queriesProps = new Properties();

	/**
	 * Constructor privado que carga los archivos de propiedades.
	 * Se asegura de inicializar todas las Properties necesarias al instanciar la clase.
	 */
	private PropertyLoader() {
		loadProperties("application.properties", applicationProps);
		loadProperties("endpoints.properties", endpointsProps);
		loadProperties("jsonKeys.properties", jsonKeysProps);
		loadProperties("queries.properties", queriesProps);
	}

	/**
	 * Holder para implementar el patrón Singleton de forma thread-safe y lazy-loaded.
	 * La instancia se crea únicamente cuando se llama por primera vez a getInstance().
	 */
	private static final class InstanceHolder {
		private static final PropertyLoader instance = new PropertyLoader();
	}

	/**
	 * Devuelve la instancia singleton de PropertyLoader.
	 *
	 * @return instancia única de PropertyLoader
	 */
	public static PropertyLoader getInstance() {
		return InstanceHolder.instance;
	}

	/**
	 * Metodo genérico para cargar un archivo de propiedades en un objeto Properties.
	 *
	 * @param fileName nombre del archivo de propiedades a cargar
	 * @param props    objeto Properties donde se cargarán las propiedades
	 * @throws IllegalStateException si el archivo no se encuentra o ocurre un error al cargarlo
	 */
	private void loadProperties(String fileName, Properties props) {
		try (InputStream input = getClass().getClassLoader()
				.getResourceAsStream(fileName)) {
			if (input == null) {
				throw new IllegalStateException(
						"No se encontró el archivo de configuración: " + fileName);
			}
			try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
				props.load(reader);
			}
		} catch (IOException e) {
			throw new IllegalStateException(
					"No se pudo cargar el archivo de configuración: " + fileName, e);
		}
	}

	// ==============================
	// Métodos para obtener objetos Properties completos
	// ==============================

	/**
	 * Devuelve todas las propiedades de application.properties
	 *
	 * @return objeto Properties completo de application.properties
	 */
	public Properties getApplicationProperties() {
		return applicationProps;
	}

	/**
	 * Devuelve todas las propiedades de endpoints.properties
	 *
	 * @return objeto Properties completo de endpoints.properties
	 */
	public Properties getEndpointsProperties() {
		return endpointsProps;
	}

	/**
	 * Devuelve todas las propiedades de jsonKeys.properties
	 *
	 * @return objeto Properties completo de jsonKeys.properties
	 */
	public Properties getJsonKeysProperties() {
		return jsonKeysProps;
	}


	// ==============================
	// Métodos para obtener propiedades
	// ==============================

	/**
	 * Obtiene el valor de una propiedad de application.properties
	 *
	 * @param key clave de la propiedad
	 * @return valor asociado a la clave o null si no existe
	 */
	public String getApplicationProperty(String key) {
		return applicationProps.getProperty(key);
	}

	/**
	 * Obtiene el valor de una propiedad de endpoints.properties
	 *
	 * @param key clave de la propiedad
	 * @return valor asociado a la clave o null si no existe
	 */
	public String getEndpointProperty(String key) {
		return endpointsProps.getProperty(key);
	}

	/**
	 * Obtiene el valor de una propiedad de jsonKeys.properties
	 *
	 * @param key clave de la propiedad
	 * @return valor asociado a la clave o null si no existe
	 */
	public String getJsonKeyProperty(String key) {
		return jsonKeysProps.getProperty(key);
	}

	/**
	 * Obtiene el valor de una propiedad de queries.properties
	 *
	 * @param key clave de la propiedad
	 * @return valor asociado a la clave o null si no existe
	 */
	public String getQuerieKeyProperty(String key) {
		return queriesProps.getProperty(key);
	}

	// =================================================
	// Métodos para obtener propiedades con valor por defecto
	// =================================================

	/**
	 * Obtiene el valor de una propiedad de application.properties, devolviendo un valor por defecto
	 * si la clave no existe.
	 *
	 * @param key          clave de la propiedad
	 * @param defaultValue valor por defecto si la propiedad no existe
	 * @return valor de la propiedad o defaultValue
	 */
	public String getApplicationProperty(String key, String defaultValue) {
		return applicationProps.getProperty(key, defaultValue);
	}

	/**
	 * Obtiene el valor de una propiedad de endpoints.properties, devolviendo un valor por defecto
	 * si la clave no existe.
	 *
	 * @param key          clave de la propiedad
	 * @param defaultValue valor por defecto si la propiedad no existe
	 * @return valor de la propiedad o defaultValue
	 */
	public String getEndpointProperty(String key, String defaultValue) {
		return endpointsProps.getProperty(key, defaultValue);
	}

	/**
	 * Obtiene el valor de una propiedad de jsonKeys.properties, devolviendo un valor por defecto
	 * si la clave no existe.
	 *
	 * @param key          clave de la propiedad
	 * @param defaultValue valor por defecto si la propiedad no existe
	 * @return valor de la propiedad o defaultValue
	 */
	public String getJsonKeyProperty(String key, String defaultValue) {
		return jsonKeysProps.getProperty(key, defaultValue);
	}
}
