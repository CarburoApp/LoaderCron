package com.inggarciabaldo.carburo.application.persistance.provincia;

import com.inggarciabaldo.carburo.application.persistance.Gateway;
import com.inggarciabaldo.carburo.application.persistance.provincia.ProvinciaGateway.ProvinciaRecord;

public interface ProvinciaGateway extends Gateway<ProvinciaRecord> {

	class ProvinciaRecord {
		public short id;
		public String denominacion;
		public short extCode;
		public short idCCAA; // FK a CCAA
	}
}
