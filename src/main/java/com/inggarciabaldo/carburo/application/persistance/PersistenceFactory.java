package com.inggarciabaldo.carburo.application.persistance;

import com.inggarciabaldo.carburo.application.persistance.ccaa.CCAAGateway;
import com.inggarciabaldo.carburo.application.persistance.ccaa.impl.CCAAGatewayImpl;
import com.inggarciabaldo.carburo.application.persistance.combustible.CombustibleGateway;
import com.inggarciabaldo.carburo.application.persistance.combustible.impl.CombustibleGatewayImpl;
import com.inggarciabaldo.carburo.application.persistance.combustibleDisponible.CombustibleDisponibleGateway;
import com.inggarciabaldo.carburo.application.persistance.combustibleDisponible.impl.CombustibleDisponibleGatewayImpl;
import com.inggarciabaldo.carburo.application.persistance.eess.EESSGateway;
import com.inggarciabaldo.carburo.application.persistance.eess.impl.EESSGatewayImpl;
import com.inggarciabaldo.carburo.application.persistance.municipio.MunicipioGateway;
import com.inggarciabaldo.carburo.application.persistance.municipio.impl.MunicipioGatewayImpl;
import com.inggarciabaldo.carburo.application.persistance.precioCombustible.PrecioCombustibleGateway;
import com.inggarciabaldo.carburo.application.persistance.precioCombustible.impl.PrecioCombustibleGatewayImpl;
import com.inggarciabaldo.carburo.application.persistance.provincia.ProvinciaGateway;
import com.inggarciabaldo.carburo.application.persistance.provincia.impl.ProvinciaGatewayImpl;

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
