package com.inggarciabaldo.carburo.application.persistance.ccaa;

import com.inggarciabaldo.carburo.application.persistance.Gateway;
import com.inggarciabaldo.carburo.application.persistance.ccaa.CCAAGateway.CCAARecord;

public interface CCAAGateway extends Gateway<CCAARecord> {

	class CCAARecord {
		public short id;
		public String denominacion;
		public short extCode;
	}
}
