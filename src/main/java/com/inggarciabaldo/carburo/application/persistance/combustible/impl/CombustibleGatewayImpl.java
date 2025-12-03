package com.inggarciabaldo.carburo.application.persistance.combustible.impl;


import com.inggarciabaldo.carburo.application.persistance.AbstractGatewayImpl;
import com.inggarciabaldo.carburo.application.persistance.combustible.CombustibleGateway;
import com.inggarciabaldo.carburo.application.persistance.combustible.CombustibleGateway.CombustibleRecord;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static com.inggarciabaldo.carburo.config.persistencia.DBColumns.COMBUSTIBLE_TABLE;

public class CombustibleGatewayImpl extends AbstractGatewayImpl<CombustibleRecord>
		implements CombustibleGateway {

	@Override
	protected String getTableName() {
		return COMBUSTIBLE_TABLE;
	}

	@Override
	protected void doInsertPreparedStatement(CombustibleRecord record,
											 PreparedStatement pst) throws SQLException {
		pst.setString(1, record.denominacion);
		pst.setString(2, record.codigo);
		pst.setInt(3, record.extCode);
	}

	@Override
	protected void doUpdatePreparedStatement(CombustibleRecord record,
											 PreparedStatement pst) throws SQLException {
		pst.setString(1, record.denominacion);
		pst.setString(2, record.codigo);
		pst.setInt(3, record.extCode);
		pst.setInt(4, record.id); // clave primaria al final
	}

	@Override
	protected List<CombustibleRecord> doFindAllTransformToList(ResultSet rs)
			throws SQLException {
		return RecordAssembler.toCombustibleRecordList(rs);
	}

	@Override
	protected Optional<CombustibleRecord> doFindByIdTransformToElement(ResultSet rs)
			throws SQLException {
		return Optional.of(RecordAssembler.toCombustibleRecord(rs));
	}

}
