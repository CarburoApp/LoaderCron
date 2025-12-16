package com.inggarciabaldo.carburo.application.persistance.preciocombustible.impl;


import com.inggarciabaldo.carburo.application.persistance.preciocombustible.PrecioCombustibleGateway.PrecioCombustibleRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.inggarciabaldo.carburo.config.persistencia.DBColumns.*;

public class RecordAssembler {

	public static PrecioCombustibleRecord toPrecioCombustibleRecord(ResultSet rs)
			throws SQLException {
		PrecioCombustibleRecord result = new PrecioCombustibleRecord();

		result.idCombustible = rs.getShort(PRECIOCOMBUSTIBLE_ID_COMBUSTIBLE);
		result.idEESS        = rs.getInt(PRECIOCOMBUSTIBLE_ID_EESS);
		result.fecha         = rs.getDate(PRECIOCOMBUSTIBLE_FECHA);
		result.precio        = rs.getDouble(PRECIOCOMBUSTIBLE_PRECIO);

		return result;
	}

	public static List<PrecioCombustibleRecord> toPrecioCombustibleRecordList(
			ResultSet rs) throws SQLException {
		List<PrecioCombustibleRecord> result = new ArrayList<>();
		while (rs.next()) {
			result.add(toPrecioCombustibleRecord(rs));
		}
		return result;
	}
}
