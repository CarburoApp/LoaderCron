package com.inggarciabaldo.carburo.application.persistance.preciocombustible.impl;

import com.inggarciabaldo.carburo.application.persistance.PersistenceException;
import com.inggarciabaldo.carburo.application.persistance.preciocombustible.PrecioCombustibleGateway;
import com.inggarciabaldo.carburo.config.persistencia.jdbc.Jdbc;
import com.inggarciabaldo.carburo.util.properties.PropertyLoader;

import java.sql.*;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.inggarciabaldo.carburo.config.persistencia.DBColumns.PRECIOCOMBUSTIBLE_TABLE;

public class PrecioCombustibleGatewayImpl implements PrecioCombustibleGateway {


	/**
	 * Cargador de propiedades para obtener consultas SQL
	 * desde los archivos .properties.
	 */
	private static final PropertyLoader propertyLoader = PropertyLoader.getInstance();

	/**
	 * Sufijos para identificar las consultas
	 */
	private static final String ADD_KEY = "ADD";
	private static final String UPDATE_KEY = "UPDATE";
	private static final String REMOVE_KEY = "REMOVE";
	private static final String FIND_ALL_KEY = "FINDALL";
	private static final String FIND_ID_KEY = "FINDBYID";
	private static final String FIND_BY_KEY = "FINDBY";

	private static final String FIND_FECHA_KEY = "FECHA";
	private static final String FIND_EESS_KEY = "EESS";
	private static final String FIND_COMBUSTIBLE_KEY = "COMBUSTIBLE";

	private String getQuery(String operation) {
		String key = PRECIOCOMBUSTIBLE_TABLE + "_" + operation;
		String value = propertyLoader.getQuerieKeyProperty(key.toUpperCase());
		if (value == null || value.isEmpty()) throw new IllegalStateException(
				"La consulta para la clave <" + key + "> no existe o está vacía.");
		return value;
	}


	@Override
	public void add(PrecioCombustibleRecord record) throws PersistenceException {
		try {
			Connection c = Jdbc.getCurrentConnection();
			String sql = getQuery(ADD_KEY);

			try (PreparedStatement pst = c.prepareStatement(sql)) {
				pst.setShort(1, record.idCombustible);
				pst.setInt(2, record.idEESS);
				pst.setDate(3, record.fecha);
				pst.setDouble(4, record.precio);
				pst.executeUpdate();
			}
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}

	/**
	 * Inserta masivamente una colección de nuevos registro de precio de combustible.
	 *
	 * @param coleccionPreciosCombustibles colección de registros a insertar
	 * @throws PersistenceException si ocurre un error de persistencia
	 */
	@Override
	public int addAll(Collection<PrecioCombustibleRecord> coleccionPreciosCombustibles) {
		int elementosInsertados = 0;
		try {
			Connection c = Jdbc.getCurrentConnection();
			String sql = getQuery(ADD_KEY);
			try (PreparedStatement pst = c.prepareStatement(sql)) {
				for (PrecioCombustibleRecord record : coleccionPreciosCombustibles) {
					pst.setShort(1, record.idCombustible);
					pst.setInt(2, record.idEESS);
					pst.setDate(3, record.fecha);
					pst.setDouble(4, record.precio);
					pst.addBatch();
				}

				int[] resultados = pst.executeBatch();

				int total = 0;
				for (int r : resultados) {
					if (r >= 0) total += r;
					else if (r == Statement.SUCCESS_NO_INFO) total++;
				}
				return total;
			}
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}

	@Override
	public void update(PrecioCombustibleRecord record) throws PersistenceException {
		try {
			Connection c = Jdbc.getCurrentConnection();
			String sql = getQuery(UPDATE_KEY);

			try (PreparedStatement pst = c.prepareStatement(sql)) {
				pst.setDouble(1, record.precio);
				pst.setShort(2, record.idCombustible);
				pst.setInt(3, record.idEESS);
				pst.setDate(4, record.fecha);
				pst.executeUpdate();
			}
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}

	@Override
	public void remove(PrecioCombustibleRecord record) throws PersistenceException {
		try {
			Connection c = Jdbc.getCurrentConnection();
			String sql = getQuery(REMOVE_KEY);

			try (PreparedStatement pst = c.prepareStatement(sql)) {
				pst.setShort(1, record.idCombustible);
				pst.setInt(2, record.idEESS);
				pst.setDate(3, record.fecha);
				pst.executeUpdate();
			}
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}

	@Override
	public List<PrecioCombustibleRecord> findAll() throws PersistenceException {
		try {
			Connection c = Jdbc.getCurrentConnection();
			String sql = getQuery(FIND_ALL_KEY);

			try (PreparedStatement pst = c.prepareStatement(sql)) {
				try (ResultSet rs = pst.executeQuery()) {
					return RecordAssembler.toPrecioCombustibleRecordList(rs);
				}
			}
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}

	@Override
	public Optional<PrecioCombustibleRecord> findById(Date fecha, int idEESS,
													  short idCombustible)
			throws PersistenceException {
		try {
			Connection c = Jdbc.getCurrentConnection();
			String sql = getQuery(FIND_ID_KEY);

			try (PreparedStatement pst = c.prepareStatement(sql)) {
				pst.setDate(1, fecha);
				pst.setInt(2, idEESS);
				pst.setShort(3, idCombustible);

				try (ResultSet rs = pst.executeQuery()) {
					return Optional.of(RecordAssembler.toPrecioCombustibleRecord(rs));
				}
			}
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}


	@Override
	public List<PrecioCombustibleRecord> findByFecha(Date fecha)
			throws PersistenceException {
		try {
			Connection c = Jdbc.getCurrentConnection();
			String sql = getQuery(FIND_BY_KEY + FIND_FECHA_KEY);
			// => FINDBYFECHA → PRECIOCOMBUSTIBLE_FINDBYFECHA
			try (PreparedStatement pst = c.prepareStatement(sql)) {

				pst.setDate(1, fecha);

				try (ResultSet rs = pst.executeQuery()) {
					return RecordAssembler.toPrecioCombustibleRecordList(rs);
				}
			}
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}

	@Override
	public List<PrecioCombustibleRecord> findByEESS(int idEESS)
			throws PersistenceException {
		try {
			Connection c = Jdbc.getCurrentConnection();
			String sql = getQuery(FIND_BY_KEY + FIND_EESS_KEY);
			// => FINDBYEESS → PRECIOCOMBUSTIBLE_FINDBYEESS

			try (PreparedStatement pst = c.prepareStatement(sql)) {
				pst.setInt(1, idEESS);

				try (ResultSet rs = pst.executeQuery()) {
					return RecordAssembler.toPrecioCombustibleRecordList(rs);
				}
			}

		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}


	@Override
	public List<PrecioCombustibleRecord> findByCombustible(short idCombustible)
			throws PersistenceException {
		try {
			Connection c = Jdbc.getCurrentConnection();
			String sql = getQuery(FIND_BY_KEY + FIND_COMBUSTIBLE_KEY);
			// => FINDBYCOMBUSTIBLE → PRECIOCOMBUSTIBLE_FINDBYCOMBUSTIBLE
			try (PreparedStatement pst = c.prepareStatement(sql)) {
				pst.setShort(1, idCombustible);

				try (ResultSet rs = pst.executeQuery()) {
					return RecordAssembler.toPrecioCombustibleRecordList(rs);
				}
			}
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}


	@Override
	public List<PrecioCombustibleRecord> findByFechaEESS(Date fecha, int idEESS)
			throws PersistenceException {
		try {
			Connection c = Jdbc.getCurrentConnection();
			String sql = getQuery(FIND_BY_KEY + FIND_FECHA_KEY + "_" + FIND_EESS_KEY);
			// => FINDBYFECHA_EESS → PRECIOCOMBUSTIBLE_FINDBYFECHA_EESS

			try (PreparedStatement pst = c.prepareStatement(sql)) {
				pst.setDate(1, fecha);
				pst.setInt(2, idEESS);
				try (ResultSet rs = pst.executeQuery()) {
					return RecordAssembler.toPrecioCombustibleRecordList(rs);
				}
			}
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}


	@Override
	public List<PrecioCombustibleRecord> findByFechaCombustible(Date fecha,
																short idCombustible)
			throws PersistenceException {
		try {
			Connection c = Jdbc.getCurrentConnection();
			String sql = getQuery(
					FIND_BY_KEY + FIND_FECHA_KEY + "_" + FIND_COMBUSTIBLE_KEY);
			// => FINDBYFECHA_COMBUSTIBLE → PRECIOCOMBUSTIBLE_FINDBYFECHA_COMBUSTIBLE
			try (PreparedStatement pst = c.prepareStatement(sql)) {
				pst.setDate(1, fecha);
				pst.setShort(2, idCombustible);

				try (ResultSet rs = pst.executeQuery()) {
					return RecordAssembler.toPrecioCombustibleRecordList(rs);
				}
			}
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}

	@Override
	public List<PrecioCombustibleRecord> findByEESSCombustible(int idEESS,
															   short idCombustible)
			throws PersistenceException {
		try {
			Connection c = Jdbc.getCurrentConnection();
			String sql = getQuery(
					FIND_BY_KEY + FIND_EESS_KEY + "_" + FIND_COMBUSTIBLE_KEY);
			// => FINDBYEESS_COMBUSTIBLE → PRECIOCOMBUSTIBLE_FINDBYEESS_COMBUSTIBLE
			try (PreparedStatement pst = c.prepareStatement(sql)) {
				pst.setInt(1, idEESS);
				pst.setShort(2, idCombustible);

				try (ResultSet rs = pst.executeQuery()) {
					return RecordAssembler.toPrecioCombustibleRecordList(rs);
				}
			}
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}

}
