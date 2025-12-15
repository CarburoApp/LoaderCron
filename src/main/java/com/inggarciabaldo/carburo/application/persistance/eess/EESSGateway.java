package com.inggarciabaldo.carburo.application.persistance.eess;

import com.inggarciabaldo.carburo.application.persistance.Gateway;
import com.inggarciabaldo.carburo.application.persistance.eess.EESSGateway.EESSRecord;

import java.util.Optional;

public interface EESSGateway extends Gateway<EESSRecord> {

	Optional<EESSRecord> findByExtCode(int extCodeId);

	class EESSRecord {
		public int id;
		public int extCode;
		public String rotulo;
		public String horario;
		public String direccion;
		public String localidad;
		public int codigoPostal;
		public short idMunicipio;
		public short idProvincia;
		public double latitud;
		public double longitud;
		public String remision;
		public double x100BioEtanol;
		public double x100EsterMetilico;
		public String margen;
		public String venta;
	}
}
