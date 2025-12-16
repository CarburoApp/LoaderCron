package com.inggarciabaldo.carburo.util.properties;

import com.inggarciabaldo.carburo.util.log.Loggers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Clase singleton para manejar propiedades de la aplicación.
 * <p>
 * Permite cargar múltiples archivos de propiedades (application, endpoints, jsonKeys, queries)
 * y acceder a sus valores de manera centralizada. Garantiza que solo exista una instancia
 * de la clase durante toda la ejecución de la aplicación.
 * <p>
 * Ahora soporta carga de propiedades desde una carpeta externa /config,
 * con fallback a resources empaquetados.
 */
public final class PropertyLoader {

	// ==============================
	// Constantes de nombres de archivos y carpetas
	// ==============================
	private static final String CONFIG_FOLDER = "config";
	private static final String APPLICATION_PROPERTIES = "application.properties";
	private static final String ENDPOINTS_PROPERTIES = "endpoints.properties";
	private static final String JSON_KEYS_PROPERTIES = "jsonKeys.properties";
	private static final String QUERIES_PROPERTIES = "queries.properties";

	// ==============================
	// Propiedades internas
	// ==============================
	private final Properties applicationProps = new Properties(); // configuración general de la aplicación
	private final Properties endpointsProps = new Properties(); // endpoints de servicios
	private final Properties jsonKeysProps = new Properties(); // claves JSON o mapeos de datos
	private final Properties queriesProps = new Properties(); // queries JDBC

	/**
	 * Constructor privado que carga los archivos de propiedades.
	 * Se asegura de inicializar todas las Properties necesarias al instanciar la clase.
	 */
	private PropertyLoader() {
		loadProperties(APPLICATION_PROPERTIES, applicationProps);
		loadProperties(ENDPOINTS_PROPERTIES, endpointsProps);
		loadProperties(JSON_KEYS_PROPERTIES, jsonKeysProps);
		loadProperties(QUERIES_PROPERTIES, queriesProps);
	}

	/**
	 * Recarga todos los archivos de propiedades de la aplicación en memoria.
	 * <p>
	 * Esta utilidad permite actualizar dinámicamente los valores de las properties
	 * sin necesidad de reiniciar la aplicación. Simplemente vuelve a cargar
	 * application.properties, endpoints.properties, jsonKeys.properties y queries.properties.
	 * </p>
	 *
	 * <b>Nota:</b> Cualquier property que se haya modificado en disco desde la carga inicial
	 * será reflejada después de invocar esta función.
	 *
	 * @throws IllegalStateException si ocurre un error al cargar alguno de los archivos
	 */
	public void reloadProperties() {
		Loggers.GENERAL.info("RECARGADA de TODAS las PROPIEDADES correctamente desde disco.");

		loadProperties(APPLICATION_PROPERTIES, applicationProps);
		loadProperties(ENDPOINTS_PROPERTIES, endpointsProps);
		loadProperties(JSON_KEYS_PROPERTIES, jsonKeysProps);
		loadProperties(QUERIES_PROPERTIES, queriesProps);
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
	 * <p>
	 * Primero intenta cargar desde la carpeta externa CONFIG_FOLDER.
	 * Si no existe, hace fallback al classpath (resources).
	 * Para application.properties, resuelve variables de entorno tipo ${VAR}.
	 */
	private void loadProperties(String fileName, Properties props) {
		props.clear(); // Limpiar antes de recargar

		Path externalPath = Paths.get(CONFIG_FOLDER, fileName);

		try {
			if (Files.exists(externalPath)) {
				// --- Carga desde carpeta externa ---
				try (Reader reader = Files.newBufferedReader(externalPath,
															 StandardCharsets.UTF_8)) {
					props.load(reader);
				}
				Loggers.GENERAL.info("PROPIEDADES correctamente CARGADAS desde disco. Ubicación: Carpeta /config");
			} else {
				// --- Fallback: carga desde resources ---
				try (InputStream input = getClass().getClassLoader()
						.getResourceAsStream(fileName)) {
					if (input == null) {
						throw new IllegalStateException(
								"No se encontró el archivo de configuración: " +
										fileName);
					}
					try (Reader reader = new InputStreamReader(input,
															   StandardCharsets.UTF_8)) {
						props.load(reader);
					}
				}
				Loggers.GENERAL.info("PROPIEDADES correctamente CARGADAS desde disco. Ubicación: archivos de respaldo en objeto compilado.");
				Loggers.GENERAL.warn("La ubicación de los archivos de properties es la de respaldo en el objeto compilado. SE TOMARAN LAS VARIABLES POR DEFECTO. Cualquier modificación en /config no se tendrá en cuenta.");
			}

			// --- Resolución de variables de entorno solo para application.properties ---
			if (APPLICATION_PROPERTIES.equals(fileName))
				for (Map.Entry<Object, Object> entry : props.entrySet()) {
					String key = entry.getKey().toString();
					String value = entry.getValue().toString();
					props.setProperty(key, resolveEnv(value));
				}
		} catch (IOException e) {
			throw new IllegalStateException(
					"No se pudo cargar el archivo de configuración: " + fileName, e);
		}
	}

	/**
	 * Sustituye patrones ${VAR} por variables de entorno reales.
	 */
	private static String resolveEnv(String value) {
		if (value == null) return null;

		Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
		Matcher matcher = pattern.matcher(value);

		StringBuilder sb = new StringBuilder();

		while (matcher.find()) {
			String varName = matcher.group(1);
			String envValue = System.getenv(varName);

			String replacement = envValue != null ? envValue : matcher.group(0);
			matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
		}

		matcher.appendTail(sb);
		return sb.toString();
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
