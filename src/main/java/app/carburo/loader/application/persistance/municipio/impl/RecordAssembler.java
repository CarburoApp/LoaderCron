package app.carburo.loader.application.persistance.municipio.impl;


import app.carburo.loader.application.persistance.municipio.MunicipioGateway.MunicipioRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static app.carburo.loader.config.persistencia.DBColumns.*;

public class RecordAssembler {

	public static MunicipioRecord toMunicipioRecord(ResultSet rs) throws SQLException {
		MunicipioRecord result = new MunicipioRecord();

		result.id           = rs.getShort(MUNICIPIO_ID);
		result.denominacion = rs.getString(MUNICIPIO_DENOMINACION);
		result.extCode      = rs.getShort(MUNICIPIO_EXT_CODE);
		result.idProvincia  = rs.getShort(MUNICIPIO_ID_PROVINCIA);

		return result;
	}

	public static List<MunicipioRecord> toMunicipioRecordList(ResultSet rs)
			throws SQLException {
		List<MunicipioRecord> result = new ArrayList<>();
		while (rs.next()) {
			result.add(toMunicipioRecord(rs));
		}
		return result;
	}
}
