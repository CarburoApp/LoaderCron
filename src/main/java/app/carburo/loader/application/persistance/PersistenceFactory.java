package app.carburo.loader.application.persistance;

import app.carburo.loader.application.persistance.ccaa.CCAAGateway;
import app.carburo.loader.application.persistance.ccaa.impl.CCAAGatewayImpl;
import app.carburo.loader.application.persistance.combustible.CombustibleGateway;
import app.carburo.loader.application.persistance.combustible.impl.CombustibleGatewayImpl;
import app.carburo.loader.application.persistance.combustibledisponible.CombustibleDisponibleGateway;
import app.carburo.loader.application.persistance.combustibledisponible.impl.CombustibleDisponibleGatewayImpl;
import app.carburo.loader.application.persistance.eess.EESSGateway;
import app.carburo.loader.application.persistance.eess.impl.EESSGatewayImpl;
import app.carburo.loader.application.persistance.municipio.MunicipioGateway;
import app.carburo.loader.application.persistance.municipio.impl.MunicipioGatewayImpl;
import app.carburo.loader.application.persistance.preciocombustible.PrecioCombustibleGateway;
import app.carburo.loader.application.persistance.preciocombustible.impl.PrecioCombustibleGatewayImpl;
import app.carburo.loader.application.persistance.provincia.ProvinciaGateway;
import app.carburo.loader.application.persistance.provincia.impl.ProvinciaGatewayImpl;

/**
 * Factoría encargada de crear instancias de los distintos Gateways
 * utilizados para el acceso a datos. Cada metodo devuelve la implementación
 * concreta del gateway para una entidad específica.
 * <p>
 * Esta clase permite centralizar la creación de objetos de persistencia,
 * facilitando su mantenimiento y sustitución futura.
 */
public class PersistenceFactory {

	/**
	 * Devuelve un gateway para acceder a los datos de las Comunidades Autónomas.
	 */
	public CCAAGateway forCCAA() {
		return new CCAAGatewayImpl();
	}

	/**
	 * Devuelve un gateway para acceder a los datos de las Provincias.
	 */
	public ProvinciaGateway forProvincia() {
		return new ProvinciaGatewayImpl();
	}

	/**
	 * Devuelve un gateway para acceder a los datos de los tipos de Combustible.
	 */
	public CombustibleGateway forCombustible() {
		return new CombustibleGatewayImpl();
	}

	/**
	 * Devuelve un gateway para acceder a los datos de las Estaciones de Servicio (EESS).
	 */
	public EESSGateway forEESS() {
		return new EESSGatewayImpl();
	}

	/**
	 * Devuelve un gateway para acceder a los datos de los Municipios.
	 */
	public MunicipioGateway forMunicipio() {
		return new MunicipioGatewayImpl();
	}

	/**
	 * Devuelve un gateway para acceder a los datos de los precios de combustible.
	 */
	public PrecioCombustibleGateway forPrecioCombustible() {
		return new PrecioCombustibleGatewayImpl();
	}

	/**
	 * Devuelve un gateway para acceder a los datos de la disponibilidad de combustible.
	 */
	public CombustibleDisponibleGateway forCombustibleDisponible() {
		return new CombustibleDisponibleGatewayImpl();
	}
}
