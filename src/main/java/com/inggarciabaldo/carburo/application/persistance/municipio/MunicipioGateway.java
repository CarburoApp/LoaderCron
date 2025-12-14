package com.inggarciabaldo.carburo.application.persistance.municipio;

import com.inggarciabaldo.carburo.application.persistance.Gateway;
import com.inggarciabaldo.carburo.application.persistance.municipio.MunicipioGateway.MunicipioRecord;

public interface MunicipioGateway extends Gateway<MunicipioRecord> {

	class MunicipioRecord {
		public short id;
		public String denominacion;
		public short extCode;
		public short idProvincia;
	}
}
