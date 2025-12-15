package com.inggarciabaldo.carburo.application.persistance.ccaa.impl;

import com.inggarciabaldo.carburo.application.persistance.AbstractGatewayImpl;
import com.inggarciabaldo.carburo.application.persistance.ccaa.CCAAGateway;
import com.inggarciabaldo.carburo.application.persistance.ccaa.CCAAGateway.CCAARecord;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static com.inggarciabaldo.carburo.config.persistencia.DBColumns.CCAA_TABLE;

public class CCAAGatewayImpl extends AbstractGatewayImpl<CCAARecord>
		implements CCAAGateway {

	@Override
	protected String getTableName() {
		return CCAA_TABLE;
	}

	@Override
	protected void doInsertPreparedStatement(CCAARecord ccaaRecord, PreparedStatement pst)
			throws SQLException {
		pst.setString(1, ccaaRecord.denominacion);
		pst.setInt(2, ccaaRecord.extCode);
	}

	@Override
	protected void doUpdatePreparedStatement(CCAARecord ccaaRecord, PreparedStatement pst)
			throws SQLException {
		pst.setString(1, ccaaRecord.denominacion);
		pst.setInt(2, ccaaRecord.extCode);
		pst.setInt(3, ccaaRecord.id);
	}

	@Override
	protected List<CCAARecord> doFindAllTransformToList(ResultSet rs)
			throws SQLException {
		return RecordAssembler.toCCAARecordList(rs);
	}

	@Override
	protected Optional<CCAARecord> doFindByIdTransformToElement(ResultSet rs)
			throws SQLException {
		if (!rs.next()) {
			return Optional.empty();
		}
		return Optional.of(RecordAssembler.toCCAARecord(rs));
	}

}
