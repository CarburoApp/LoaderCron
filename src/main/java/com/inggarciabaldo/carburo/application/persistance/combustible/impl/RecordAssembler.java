package com.inggarciabaldo.carburo.application.persistance.combustible.impl;

import com.inggarciabaldo.carburo.application.persistance.combustible.CombustibleGateway.CombustibleRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.inggarciabaldo.carburo.config.persistencia.DBColumns.*;

public class RecordAssembler {

	public static CombustibleRecord toCombustibleRecord(ResultSet rs)
			throws SQLException {
		CombustibleRecord result = new CombustibleRecord();

		result.id           = rs.getShort(COMBUSTIBLE_ID);
		result.denominacion = rs.getString(COMBUSTIBLE_DENOMINACION);
		result.codigo       = rs.getString(COMBUSTIBLE_CODIGO);
		result.extCode      = rs.getShort(COMBUSTIBLE_EXT_CODE);

		return result;
	}

	public static List<CombustibleRecord> toCombustibleRecordList(ResultSet rs)
			throws SQLException {
		List<CombustibleRecord> result = new ArrayList<>();
		while (rs.next()) {
			result.add(toCombustibleRecord(rs));
		}
		return result;
	}
}
