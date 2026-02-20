package app.carburo.loader.application.persistance.provincia;

import app.carburo.loader.application.persistance.Gateway;
import app.carburo.loader.application.persistance.provincia.ProvinciaGateway.ProvinciaRecord;

public interface ProvinciaGateway extends Gateway<ProvinciaRecord> {

	class ProvinciaRecord {
		public short id;
		public String denominacion;
		public short extCode;
		public short idCCAA; // FK a CCAA
	}
}
