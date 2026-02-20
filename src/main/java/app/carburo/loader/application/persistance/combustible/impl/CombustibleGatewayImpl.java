package app.carburo.loader.application.persistance.combustible.impl;


import app.carburo.loader.application.persistance.AbstractGatewayImpl;
import app.carburo.loader.application.persistance.combustible.CombustibleGateway;
import app.carburo.loader.application.persistance.combustible.CombustibleGateway.CombustibleRecord;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static app.carburo.loader.config.persistencia.DBColumns.COMBUSTIBLE_TABLE;

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
		if (!rs.next()) {
			return Optional.empty();
		}
		return Optional.of(RecordAssembler.toCombustibleRecord(rs));
	}

}
