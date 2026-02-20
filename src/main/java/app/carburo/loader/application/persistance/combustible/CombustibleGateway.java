package app.carburo.loader.application.persistance.combustible;

import app.carburo.loader.application.persistance.Gateway;
import app.carburo.loader.application.persistance.combustible.CombustibleGateway.CombustibleRecord;

public interface CombustibleGateway extends Gateway<CombustibleRecord> {

	class CombustibleRecord {
		public short id;
		public String denominacion;
		public String codigo;
		public short extCode;
	}
}
