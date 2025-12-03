package com.inggarciabaldo.carburo.application.persistance.combustible;

import com.inggarciabaldo.carburo.application.persistance.Gateway;
import com.inggarciabaldo.carburo.application.persistance.combustible.CombustibleGateway.CombustibleRecord;

public interface CombustibleGateway extends Gateway<CombustibleRecord> {

	class CombustibleRecord {
		public short id;
		public String denominacion;
		public String codigo;
		public short extCode;
	}
}
