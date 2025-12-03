package com.inggarciabaldo.carburo.application.persistance.combustibleDisponible.impl;


import com.inggarciabaldo.carburo.application.persistance.combustibleDisponible.CombustibleDisponibleGateway.CombustibleDisponibleRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.inggarciabaldo.carburo.application.persistance.DBColumns.*;

public class RecordAssembler {

	public static CombustibleDisponibleRecord toCombustibleDisponibleRecord(ResultSet rs)
			throws SQLException {
		CombustibleDisponibleRecord result = new CombustibleDisponibleRecord();

		result.idEESS        = rs.getInt(COMBUSTIBLE_DISPONIBLE_ID_EESS);
		result.idCombustible = rs.getShort(COMBUSTIBLE_DISPONIBLE_ID_COMBUSTIBLE);

		return result;
	}

	public static List<CombustibleDisponibleRecord> toCombustibleDisponibleRecordList(
			ResultSet rs) throws SQLException {
		List<CombustibleDisponibleRecord> result = new ArrayList<>();
		while (rs.next()) {
			result.add(toCombustibleDisponibleRecord(rs));
		}
		return result;
	}
}
