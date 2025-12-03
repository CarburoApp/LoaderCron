package com.inggarciabaldo.carburo.application.persistance.eess;

import com.inggarciabaldo.carburo.application.persistance.Gateway;
import com.inggarciabaldo.carburo.application.persistance.eess.EESSGateway.EESSRecord;

public interface EESSGateway extends Gateway<EESSRecord> {

	class EESSRecord {
		public int id;
		public int extCode;
		public String rotulo;
		public String horario;
		public String direccion;
		public String localidad;
		public Integer codigoPostal;
		public Short idMunicipio;
		public Short idProvincia;
		public Double latitud;
		public Double longitud;
		public String remision;
		public Double x100BioEtanol;
		public Double x100EsterMetilico;
		public String margen;
		public String venta;
	}
}
