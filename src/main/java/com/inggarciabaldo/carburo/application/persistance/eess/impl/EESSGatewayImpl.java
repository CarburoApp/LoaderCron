package com.inggarciabaldo.carburo.application.persistance.eess.impl;

import com.inggarciabaldo.carburo.application.persistance.AbstractGatewayImpl;
import com.inggarciabaldo.carburo.application.persistance.PersistenceException;
import com.inggarciabaldo.carburo.application.persistance.eess.EESSGateway;
import com.inggarciabaldo.carburo.application.persistance.eess.EESSGateway.EESSRecord;
import com.inggarciabaldo.carburo.config.persistencia.jdbc.Jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static com.inggarciabaldo.carburo.config.persistencia.DBColumns.EESS_TABLE;

public class EESSGatewayImpl extends AbstractGatewayImpl<EESSRecord>
		implements EESSGateway {

	/**
	 * Sufijos estándar para identificar las consultas
	 * EESS_FINDBYEXTCODE
	 */
	private final static String FIND_EXTCODE_KEY = "FINDBYEXTCODE";

	@Override
	public Optional<EESSRecord> findByExtCode(int extCodeId) {
		try {
			Connection c = Jdbc.getCurrentConnection();
			String sql = getQuery(FIND_EXTCODE_KEY);

			try (PreparedStatement pst = c.prepareStatement(sql)) {
				pst.setInt(1, extCodeId);

				try (ResultSet rs = pst.executeQuery()) {
					return doFindByIdTransformToElement(rs);
				}
			}
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}

	@Override
	protected String getTableName() {
		return EESS_TABLE;
	}

	@Override
	protected void doInsertPreparedStatement(EESSRecord record, PreparedStatement pst)
			throws SQLException {
		pst.setString(1, record.rotulo);
		pst.setString(2, record.horario);
		pst.setString(3, record.direccion);
		pst.setString(4, record.localidad);
		pst.setInt(5, record.codigoPostal);
		pst.setInt(6, record.idMunicipio);
		pst.setInt(7, record.idProvincia);
		pst.setDouble(8, record.longitud);  // ST_MakePoint(lon, lat) 1º Long
		pst.setDouble(9, record.latitud);  // ST_MakePoint(lon, lat)
		pst.setString(10, record.remision);
		pst.setDouble(11, record.x100BioEtanol);
		pst.setDouble(12, record.x100EsterMetilico);
		pst.setString(13, record.margen);
		pst.setString(14, record.venta);
		pst.setInt(15, record.extCode);
	}

	@Override
	protected void doUpdatePreparedStatement(EESSRecord record, PreparedStatement pst)
			throws SQLException {
		doInsertPreparedStatement(record, pst);
		pst.setInt(16, record.id); // clave primaria al final
	}

	@Override
	protected List<EESSRecord> doFindAllTransformToList(ResultSet rs)
			throws SQLException {
		if (!rs.next()) {
			return List.of();
		}
		return RecordAssembler.toEESSRecordList(rs);
	}

	@Override
	protected Optional<EESSRecord> doFindByIdTransformToElement(ResultSet rs)
			throws SQLException {
		if (!rs.next()) {
			return Optional.empty();
		}
		return Optional.of(RecordAssembler.toEESSRecord(rs));
	}
}
