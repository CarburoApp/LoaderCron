package com.inggarciabaldo.carburo.application.persistance.eess.impl;

import com.inggarciabaldo.carburo.application.persistance.eess.EESSGateway.EESSRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.inggarciabaldo.carburo.application.persistance.DBColumns.*;

public class RecordAssembler {

	public static EESSRecord toEESSRecord(ResultSet rs) throws SQLException {
		EESSRecord result = new EESSRecord();

		result.id                = rs.getInt(EESS_ID);
		result.extCode           = rs.getInt(EESS_EXT_CODE);
		result.rotulo            = rs.getString(EESS_ROTULO);
		result.horario           = rs.getString(EESS_HORARIO);
		result.direccion         = rs.getString(EESS_DIRECCION);
		result.localidad         = rs.getString(EESS_LOCALIDAD);
		result.codigoPostal      = rs.getInt(EESS_CODIGO_POSTAL);
		result.idMunicipio       = rs.getShort(EESS_ID_MUNICIPIO);
		result.idProvincia       = rs.getShort(EESS_ID_PROVINCIA);
		result.latitud           = rs.getDouble(EESS_LATITUD); // ahora viene de ST_Y
		result.longitud          = rs.getDouble(EESS_LONGITUD); // ahora viene de ST_X
		result.remision          = rs.getString(EESS_REMISION);
		result.x100BioEtanol     = rs.getDouble(EESS_X100_BIO_ETANOL);
		result.x100EsterMetilico = rs.getDouble(EESS_X100_ESTER_METILICO);
		result.margen            = rs.getString(EESS_MARGEN);
		result.venta             = rs.getString(EESS_VENTA);

		return result;
	}

	public static List<EESSRecord> toEESSRecordList(ResultSet rs) throws SQLException {
		List<EESSRecord> result = new ArrayList<>();
		while (rs.next()) {
			result.add(toEESSRecord(rs));
		}
		return result;
	}
}
