package com.inggarciabaldo.carburo.application.persistance.municipio.impl;

import com.inggarciabaldo.carburo.application.persistance.AbstractGatewayImpl;
import com.inggarciabaldo.carburo.application.persistance.municipio.MunicipioGateway;
import com.inggarciabaldo.carburo.application.persistance.municipio.MunicipioGateway.MunicipioRecord;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static com.inggarciabaldo.carburo.application.persistance.DBColumns.MUNICIPIO_TABLE;

public class MunicipioGatewayImpl extends AbstractGatewayImpl<MunicipioRecord>
		implements MunicipioGateway {

	@Override
	protected String getTableName() {
		return MUNICIPIO_TABLE;
	}

	@Override
	protected void doInsertPreparedStatement(MunicipioRecord record,
											 PreparedStatement pst) throws SQLException {
		pst.setString(1, record.denominacion);
		pst.setInt(2, record.extCode);
		pst.setInt(3, record.idProvincia);
	}

	@Override
	protected void doUpdatePreparedStatement(MunicipioRecord record,
											 PreparedStatement pst) throws SQLException {
		doInsertPreparedStatement(record, pst);
		pst.setInt(4, record.id); // clave primaria al final
	}

	@Override
	protected List<MunicipioRecord> doFindAllTransformToList(ResultSet rs)
			throws SQLException {
		return RecordAssembler.toMunicipioRecordList(rs);
	}

	@Override
	protected Optional<MunicipioRecord> doFindByIdTransformToElement(ResultSet rs)
			throws SQLException {
		return Optional.of(RecordAssembler.toMunicipioRecord(rs));
	}
}
