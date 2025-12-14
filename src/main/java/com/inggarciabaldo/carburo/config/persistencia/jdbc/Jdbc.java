package com.inggarciabaldo.carburo.config.persistencia.jdbc;

import com.inggarciabaldo.carburo.util.log.Loggers;
import com.inggarciabaldo.carburo.util.properties.PropertyLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase de utilidad para gestionar conexiones JDBC a la base de datos.
 * <p>
 * Esta clase permite crear y almacenar conexiones por hilo usando un {@link ThreadLocal}.
 * Se encarga de obtener los datos de conexión (URL, usuario, contraseña) a partir de
 * la configuración de la aplicación mediante {@link PropertyLoader}.
 * </p>
 * <p>
 * IMPORTANTE:
 * - Cada hilo debe cerrar la conexión obtenida al finalizar su uso para evitar fugas de recursos.
 * - No se recomienda ejecutar operaciones de actualización (executeUpdate) sin un manejo
 * adecuado de transacciones.
 * </p>
 */
public class Jdbc {

	/**
	 * Instancia singleton del cargador de propiedades de la aplicación
	 */
	private final static PropertyLoader config = PropertyLoader.getInstance();

	/**
	 * Clave de la propiedad que contiene la URL de la base de datos
	 */
	private static final String URL_PROPERTY = "DB_URL";

	/**
	 * Clave de la propiedad que contiene el usuario de la base de datos
	 */
	private static final String USER_PROPERTY = "DB_USER";

	/**
	 * Clave de la propiedad que contiene la contraseña de la base de datos
	 */
	private static final String PASS_PROPERTY = "DB_PASS";

	/**
	 * Almacena la conexión JDBC asociada a cada hilo.
	 * ThreadLocal asegura que cada hilo tenga su propia conexión independiente.
	 */
	private static final ThreadLocal<Connection> threadConnection = new ThreadLocal<>();

	/**
	 * Crea una nueva conexión JDBC para el hilo actual y la almacena en {@link #threadConnection}.
	 *
	 * @return La conexión JDBC creada para el hilo actual.
	 * @throws SQLException Si ocurre un error al establecer la conexión con la base de datos.
	 * @implNote Se recomienda cerrar la conexión manualmente cuando el hilo termine de usarla.
	 * Ejemplo:
	 * <pre>
	 *               Connection con = Jdbc.createThreadConnection();
	 *               try {
	 *                   // operaciones con la conexión
	 *               } finally {
	 *                   con.close();
	 *               }
	 *           </pre>
	 */
	public static Connection createThreadConnection() throws SQLException {
		Connection con = DriverManager.getConnection(
				config.getApplicationProperty(URL_PROPERTY),
				config.getApplicationProperty(USER_PROPERTY),
				config.getApplicationProperty(PASS_PROPERTY));
		threadConnection.set(con);
		Loggers.DB.info("[JDBC] Conexión creada para el hilo: {}",
						Thread.currentThread().getName());
		return con;
	}

	/**
	 * Obtiene la conexión JDBC asociada al hilo actual.
	 *
	 * @return La conexión JDBC del hilo actual, o {@code null} si no se ha creado ninguna.
	 */
	public static Connection getCurrentConnection() {
		return threadConnection.get();
	}

	/**
	 * Comprueba si se puede establecer una conexión con la base de datos.
	 * <p>
	 * Este metodo crea una conexión independiente (no usa el ThreadLocal)
	 * únicamente para verificar la conectividad. La conexión se cierra
	 * automáticamente al salir del bloque try-with-resources.
	 * </p>
	 *
	 * @return true si la conexión se establece correctamente; false si ocurre un error.
	 */
	public static boolean testConnection() {
		try (Connection con = DriverManager.getConnection(
				config.getApplicationProperty(URL_PROPERTY),
				config.getApplicationProperty(USER_PROPERTY),
				config.getApplicationProperty(PASS_PROPERTY))) {

			Loggers.DB.info("Conexión de prueba establecida correctamente.");
			return true;

		} catch (SQLException e) {
			Loggers.DB.error("Error al comprobar la conexión a la base de datos: {}",
							 e.getMessage());
			return false;
		}
	}

}
