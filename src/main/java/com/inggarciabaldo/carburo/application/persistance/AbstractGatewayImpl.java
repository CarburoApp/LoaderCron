package com.inggarciabaldo.carburo.application.persistance;

import com.inggarciabaldo.carburo.config.persistencia.jdbc.Jdbc;
import com.inggarciabaldo.carburo.util.properties.PropertyLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementación base (abstracta) del patrón Gateway.
 * Proporciona la lógica común para CRUD mediante JDBC,
 * dejando que cada subclase defina cómo preparar los datos
 * y cómo transformar los ResultSet en objetos del dominio.
 *
 * @param <T> Tipo de entidad gestionada por el Gateway.
 */
public abstract class AbstractGatewayImpl<T> implements Gateway<T> {

	/**
	 * Cargador de propiedades para obtener consultas SQL
	 * desde los archivos .properties.
	 */
	private final static PropertyLoader propertyLoader = PropertyLoader.getInstance();

	/**
	 * Sufijos estándar para identificar las consultas
	 * dentro del archivo .properties.
	 * <p>
	 * Por ejemplo:
	 * USUARIO_ADD
	 * USUARIO_UPDATE
	 * USUARIO_FINDALL
	 * USUARIO_FINDBYID
	 */
	private final static String ADD_KEY = "ADD";
	private final static String UPDATE_KEY = "UPDATE";
	private final static String FIND_ALL_KEY = "FINDALL";
	private final static String FIND_ID_KEY = "FINDBYID";


	/*
	 * ===================================================================
	 *       MÉTODOS QUE LAS SUBCLASES DEBEN IMPLEMENTAR OBLIGATORIAMENTE
	 * ===================================================================
	 */

	/**
	 * Debe devolver el nombre de la tabla asociada.
	 * Este nombre se usa para obtener las consultas SQL del properties.
	 */
	protected abstract String getTableName();

	/**
	 * Configura el PreparedStatement para una inserción.
	 * No debe ejecutar ni commit, solo asignar parámetros.
	 *
	 * @throws SQLException si falla al asignar los parámetros.
	 */
	protected abstract void doInsertPreparedStatement(T t, PreparedStatement pst)
			throws SQLException;

	/**
	 * Configura el PreparedStatement para una actualización.
	 * No debe ejecutar la consulta.
	 *
	 * @throws SQLException si falla al asignar los parámetros.
	 */
	protected abstract void doUpdatePreparedStatement(T t, PreparedStatement pst)
			throws SQLException;

	/**
	 * Convierte un ResultSet completo en una lista de objetos.
	 *
	 * @throws SQLException si ocurre un error leyendo el ResultSet.
	 */
	protected abstract List<T> doFindAllTransformToList(ResultSet rs) throws SQLException;

	/**
	 * Convierte un ResultSet en un único elemento (si existe).
	 *
	 * @throws SQLException si ocurre un error leyendo el ResultSet.
	 */
	protected abstract Optional<T> doFindByIdTransformToElement(ResultSet rs)
			throws SQLException;


	/**
	 * Obtiene la consulta SQL correspondiente a una operación para esta tabla.
	 * Las claves se construyen como NOMBRETABLA_OPERACION.
	 */
	private String getQuery(String operation) {
		String key = getTableName() + "_" + operation;
		String value = propertyLoader.getQuerieKeyProperty(key.toUpperCase());
		if (value == null || value.isEmpty()) throw new IllegalStateException(
				"La consulta para la clave <" + key + "> no existe o está vacía.");
		return value;
	}


	/*
	 * ==========================================================
	 *                     MÉTODOS FINALES CRUD
	 *     (La subclase NO puede sobreescribirlos)
	 * ==========================================================
	 */

	/**
	 * Inserta un elemento en la base de datos.
	 * <p>
	 * Pasos:
	 * 1) Obtiene la conexión actual del ThreadLocal.
	 * 2) Carga la consulta SQL desde el properties.
	 * 3) Prepara el PreparedStatement.
	 * 4) La subclase asigna los parámetros.
	 * 5) Se ejecuta la inserción.
	 * <p>
	 * Excepciones:
	 * SQLException → Se envuelve en PersistenceException.
	 * <p>
	 * Importante:
	 * - La subclase NO debe ejecutar pst.executeUpdate().
	 * - Solo asignar valores a los parámetros.
	 *
	 * @throws PersistenceException si ocurre un error de persistencia.
	 */
	@Override
	public final Long add(T t) throws PersistenceException {
		try {
			Connection c = Jdbc.getCurrentConnection();
			String sql = getQuery(ADD_KEY);

			// Preparar statement indicando que queremos la clave generada
			try (PreparedStatement pst = c.prepareStatement(sql,
															PreparedStatement.RETURN_GENERATED_KEYS)) {
				// La subclase solo asigna parámetros
				doInsertPreparedStatement(t, pst);
				pst.executeUpdate();

				// Leer el id autogenerado
				try (ResultSet rs = pst.getGeneratedKeys()) {
					if (rs.next()) {
						return rs.getLong(1); // devuelve el primer campo generado
					} else {
						throw new PersistenceException(
								"No se pudo obtener el ID generado");
					}
				}
			}
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}

	/**
	 * Actualiza un elemento existente en la base de datos.
	 * <p>
	 * Excepciones:
	 * - SQLException → envuelta en PersistenceException.
	 *
	 * <p>
	 * La subclase:
	 * - Debe asignar valores al PreparedStatement.
	 * - NO debe ejecutar la consulta.
	 *
	 * @throws PersistenceException si ocurre un error de persistencia.
	 */
	@Override
	public final void update(T t) throws PersistenceException {
		try {
			Connection c = Jdbc.getCurrentConnection();
			String sql = getQuery(UPDATE_KEY);

			try (PreparedStatement pst = c.prepareStatement(sql)) {
				doUpdatePreparedStatement(t, pst);
				pst.executeUpdate();
			}
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}

	/**
	 * Busca un elemento por su ID.
	 * <p>
	 * Excepciones:
	 * - SQLException → envuelta en PersistenceException.
	 *
	 * <p>
	 * La subclase:
	 * - Debe recorrer el ResultSet y transformarlo en Optional<T>.
	 * - NO debe ejecutar consultas ni cerrar el ResultSet.
	 *
	 * @throws PersistenceException si ocurre un error de persistencia.
	 */
	@Override
	public Optional<T> findById(String id) throws PersistenceException {
		try {
			Connection c = Jdbc.getCurrentConnection();
			String sql = getQuery(FIND_ID_KEY);

			try (PreparedStatement pst = c.prepareStatement(sql)) {
				pst.setString(1, id);

				try (ResultSet rs = pst.executeQuery()) {
					return doFindByIdTransformToElement(rs);
				}
			}
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}

	/**
	 * Obtiene todos los elementos de la tabla.
	 * <p>
	 * Excepciones:
	 * - SQLException → envuelta en PersistenceException.
	 *
	 *
	 * <p>
	 * La subclase:
	 * - Transforma el ResultSet en una lista.
	 *
	 * @throws PersistenceException si ocurre un error de persistencia.
	 */
	@Override
	public List<T> findAll() throws PersistenceException {
		try {
			Connection c = Jdbc.getCurrentConnection();
			String sql = getQuery(FIND_ALL_KEY);

			try (PreparedStatement pst = c.prepareStatement(sql)) {
				try (ResultSet rs = pst.executeQuery()) {
					return doFindAllTransformToList(rs);
				}
			}
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}
}
