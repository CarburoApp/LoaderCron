package app.carburo.loader.application.persistance.municipio;

import app.carburo.loader.application.persistance.Gateway;
import app.carburo.loader.application.persistance.municipio.MunicipioGateway.MunicipioRecord;

public interface MunicipioGateway extends Gateway<MunicipioRecord> {

	class MunicipioRecord {
		public short id;
		public String denominacion;
		public short extCode;
		public short idProvincia;
	}
}
