package com.inggarciabaldo.carburo.application.persistance.ccaa.impl;

import com.inggarciabaldo.carburo.application.persistance.ccaa.CCAAGateway.CCAARecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.inggarciabaldo.carburo.config.persistencia.DBColumns.*;

public class RecordAssembler {

	public static CCAARecord toCCAARecord(ResultSet rs) throws SQLException {
		CCAARecord result = new CCAARecord();

		result.id           = rs.getInt(CCAA_ID);
		result.denominacion = rs.getString(CCAA_DENOMINACION);
		result.extCode      = rs.getInt(CCAA_EXT_CODE);

		return result;
	}

	public static List<CCAARecord> toCCAARecordList(ResultSet rs) throws SQLException {
		List<CCAARecord> result = new ArrayList<>();
		while (rs.next()) {
			result.add(toCCAARecord(rs));
		}
		return result;
	}
}
