package app.carburo.loader.application.persistance.provincia.impl;


import app.carburo.loader.application.persistance.provincia.ProvinciaGateway.ProvinciaRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static app.carburo.loader.config.persistencia.DBColumns.*;

public class RecordAssembler {

	public static ProvinciaRecord toProvinciaRecord(ResultSet rs) throws SQLException {
		ProvinciaRecord result = new ProvinciaRecord();

		result.id           = rs.getShort(PROVINCIA_ID);
		result.denominacion = rs.getString(PROVINCIA_DENOMINACION);
		result.extCode      = rs.getShort(PROVINCIA_EXT_CODE);
		result.idCCAA       = rs.getShort(PROVINCIA_ID_CCAA);

		return result;
	}

	public static List<ProvinciaRecord> toProvinciaRecordList(ResultSet rs)
			throws SQLException {
		List<ProvinciaRecord> result = new ArrayList<>();
		while (rs.next()) {
			result.add(toProvinciaRecord(rs));
		}
		return result;
	}
}
