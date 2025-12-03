package com.inggarciabaldo.carburo.application.persistance.provincia.impl;

import com.inggarciabaldo.carburo.application.persistance.AbstractGatewayImpl;
import com.inggarciabaldo.carburo.application.persistance.provincia.ProvinciaGateway;
import com.inggarciabaldo.carburo.application.persistance.provincia.ProvinciaGateway.ProvinciaRecord;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static com.inggarciabaldo.carburo.config.persistencia.DBColumns.PROVINCIA_TABLE;

public class ProvinciaGatewayImpl extends AbstractGatewayImpl<ProvinciaRecord>
		implements ProvinciaGateway {

	@Override
	protected String getTableName() {
		return PROVINCIA_TABLE;
	}

	@Override
	protected void doInsertPreparedStatement(ProvinciaRecord record,
											 PreparedStatement pst) throws SQLException {
		pst.setString(1, record.denominacion);
		pst.setInt(2, record.extCode);
		pst.setInt(3, record.idCCAA);
	}

	@Override
	protected void doUpdatePreparedStatement(ProvinciaRecord record,
											 PreparedStatement pst) throws SQLException {
		doInsertPreparedStatement(record, pst);
		pst.setInt(4, record.id); // clave primaria al final
	}

	@Override
	protected List<ProvinciaRecord> doFindAllTransformToList(ResultSet rs)
			throws SQLException {
		return RecordAssembler.toProvinciaRecordList(rs);
	}

	@Override
	protected Optional<ProvinciaRecord> doFindByIdTransformToElement(ResultSet rs)
			throws SQLException {
		return Optional.of(RecordAssembler.toProvinciaRecord(rs));
	}
}
