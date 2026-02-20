package app.carburo.loader.application.persistance.eess;

import app.carburo.loader.application.persistance.Gateway;
import app.carburo.loader.application.persistance.PersistenceException;
import app.carburo.loader.application.persistance.eess.EESSGateway.EESSRecord;

import java.util.Collection;
import java.util.Optional;

public interface EESSGateway extends Gateway<EESSRecord> {

	Optional<EESSRecord> findByExtCode(int extCodeId);

	/**
	 * Inserta masivamente una colección de nuevas EESS.
	 *
	 * @param records colección de EESS a insertar
	 * @return los records de las EESS insertadas satisfactoriamente con el id asignado.
	 * @throws PersistenceException si ocurre un error de persistencia
	 */
	Collection<EESSRecord> addAll(Collection<EESSRecord> records);

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
