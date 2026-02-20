package app.carburo.loader.application.persistance.ccaa;

import app.carburo.loader.application.persistance.Gateway;
import app.carburo.loader.application.persistance.ccaa.CCAAGateway.CCAARecord;

public interface CCAAGateway extends Gateway<CCAARecord> {

	class CCAARecord {
		public short id;
		public String denominacion;
		public short extCode;
	}
}
