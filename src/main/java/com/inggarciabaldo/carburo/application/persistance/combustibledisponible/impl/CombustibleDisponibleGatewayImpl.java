package com.inggarciabaldo.carburo.application.persistance.combustibledisponible.impl;

import com.inggarciabaldo.carburo.application.persistance.PersistenceException;
import com.inggarciabaldo.carburo.application.persistance.combustibledisponible.CombustibleDisponibleGateway;
import com.inggarciabaldo.carburo.config.persistencia.jdbc.Jdbc;
import com.inggarciabaldo.carburo.util.properties.PropertyLoader;

import java.sql.*;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.inggarciabaldo.carburo.config.persistencia.DBColumns.COMBUSTIBLE_DISPONIBLE_TABLE;

public class CombustibleDisponibleGatewayImpl implements CombustibleDisponibleGateway {


	/**
	 * Cargador de propiedades para obtener consultas SQL
	 * desde los archivos .properties.
	 */
	private static final PropertyLoader propertyLoader = PropertyLoader.getInstance();

	/**
	 * Sufijos para identificar las consultas
	 */
	private static final String ADD_KEY = "ADD";
	private static final String REMOVE_KEY = "REMOVE";
	private static final String FIND_ALL_KEY = "FINDALL";
	private static final String FIND_ID_KEY = "FINDBYID";
	private static final String FIND_BY_KEY = "FINDBY";

	private static final String FIND_EESS_KEY = "EESS";
	private static final String FIND_COMBUSTIBLE_KEY = "COMBUSTIBLE";

	private String getQuery(String operation) {
		String key = COMBUSTIBLE_DISPONIBLE_TABLE + "_" + operation;
		String value = propertyLoader.getQuerieKeyProperty(key.toUpperCase());
		if (value == null || value.isEmpty()) throw new IllegalStateException(
				"La consulta para la clave <" + key + "> no existe o está vacía.");
		return value;
	}


	@Override
	public void add(short idCombustible, int idEESS) throws PersistenceException {
		try {
			Connection c = Jdbc.getCurrentConnection();
			String sql = getQuery(ADD_KEY);

			try (PreparedStatement pst = c.prepareStatement(sql)) {
				pst.setShort(1, idCombustible);
				pst.setInt(2, idEESS);
				pst.executeUpdate();
			}
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}

	/**
	 * Inserta masivamente una colección de objetos Combustible-Disponible.
	 *
	 * @param records colección de EESS a insertar
	 * @return número de objetos Combustible-Disponible insertados satisfactoriamente
	 * @throws PersistenceException si ocurre un error de persistencia
	 */
	@Override
	public int addAll(Collection<CombustibleDisponibleRecord> records) {
		try {
			Connection c = Jdbc.getCurrentConnection();
			String sql = getQuery(ADD_KEY);

			try (PreparedStatement pst = c.prepareStatement(sql)) {
				for (CombustibleDisponibleRecord record : records) {
					pst.setShort(1, record.idCombustible);
					pst.setInt(2, record.idEESS);
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
	public void remove(short idCombustible, int idEESS) throws PersistenceException {
		try {
			Connection c = Jdbc.getCurrentConnection();
			String sql = getQuery(REMOVE_KEY);

			try (PreparedStatement pst = c.prepareStatement(sql)) {
				pst.setShort(1, idCombustible);
				pst.setInt(2, idEESS);
				pst.executeUpdate();
			}
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}

	@Override
	public List<CombustibleDisponibleRecord> findAll() throws PersistenceException {
		try {
			Connection c = Jdbc.getCurrentConnection();
			String sql = getQuery(FIND_ALL_KEY);

			try (PreparedStatement pst = c.prepareStatement(sql)) {
				try (ResultSet rs = pst.executeQuery()) {
					return com.inggarciabaldo.carburo.application.persistance.combustibledisponible.impl.RecordAssembler.toCombustibleDisponibleRecordList(
							rs);
				}
			}
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}

	@Override
	public Optional<CombustibleDisponibleRecord> findById(short idCombustible, int idEESS)
			throws PersistenceException {
		try {
			Connection c = Jdbc.getCurrentConnection();
			String sql = getQuery(FIND_ID_KEY);

			try (PreparedStatement pst = c.prepareStatement(sql)) {
				pst.setShort(1, idCombustible);
				pst.setInt(2, idEESS);

				try (ResultSet rs = pst.executeQuery()) {
					return Optional.of(RecordAssembler.toCombustibleDisponibleRecord(rs));
				}
			}
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}

	@Override
	public List<CombustibleDisponibleRecord> findByEESS(int idEESS)
			throws PersistenceException {
		try {
			Connection c = Jdbc.getCurrentConnection();
			String sql = getQuery(FIND_BY_KEY + FIND_EESS_KEY);
			try (PreparedStatement pst = c.prepareStatement(sql)) {
				pst.setInt(1, idEESS);

				try (ResultSet rs = pst.executeQuery()) {
					return RecordAssembler.toCombustibleDisponibleRecordList(rs);
				}
			}

		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}


	@Override
	public List<CombustibleDisponibleRecord> findByCombustible(short idCombustible)
			throws PersistenceException {
		try {
			Connection c = Jdbc.getCurrentConnection();
			String sql = getQuery(FIND_BY_KEY + FIND_COMBUSTIBLE_KEY);
			try (PreparedStatement pst = c.prepareStatement(sql)) {
				pst.setShort(1, idCombustible);

				try (ResultSet rs = pst.executeQuery()) {
					return RecordAssembler.toCombustibleDisponibleRecordList(rs);
				}
			}
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}

}
