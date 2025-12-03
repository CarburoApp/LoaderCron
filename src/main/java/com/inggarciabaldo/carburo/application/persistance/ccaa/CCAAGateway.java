package com.inggarciabaldo.carburo.application.persistance.ccaa;

import com.inggarciabaldo.carburo.application.persistance.Gateway;
import com.inggarciabaldo.carburo.application.persistance.ccaa.CCAAGateway.CCAARecord;

public interface CCAAGateway extends Gateway<CCAARecord> {

	class CCAARecord {
		public int id;
		public String denominacion;
		public int extCode;
	}
}
