package com.inggarciabaldo.carburo.application.rest.parser;

import com.inggarciabaldo.carburo.application.Factorias;
import com.inggarciabaldo.carburo.application.model.*;
import com.inggarciabaldo.carburo.application.model.enums.Margen;
import com.inggarciabaldo.carburo.application.model.enums.Remision;
import com.inggarciabaldo.carburo.application.model.enums.Venta;
import com.inggarciabaldo.carburo.application.rest.dto.ESParserDTO;
import com.inggarciabaldo.carburo.application.rest.dto.PreciosCombustibleParserDTO;
import com.inggarciabaldo.carburo.application.service.ServiceFactory;
import com.inggarciabaldo.carburo.util.log.Loggers;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Parser específico para estaciones de servicio (EESS).
 */
public class EESSParser {

	// Logger para operaciones de parseo
	private static final Logger parseLog = Loggers.PARSE;

	private final PreciosCombustibleParser preciosCombustibleParser;

	// Mapas de referencia para evitar consultas repetidas
	private final Map<Short, Provincia> provinciasMap;
	private final Map<Short, Municipio> municipiosMap;


	/**
	 * Constructor del parser de EESS.
	 *<p>
	 * Se encarga de cargar los datos iniciales de referencia necesarios para el parseo.
	 * Estos datos los tomará de la BD o de la Caché según corresponda.
	 *<p>
	 * Se recomienda encarecidamente usar la caché para optimizar el rendimiento.
	 */
	public EESSParser() {

		ServiceFactory serviceFactory = Factorias.service;

		// Crear los mapas de referencia para el parser
		Set<Combustible> combustibles = new HashSet<>(
				serviceFactory.forCombustible().findAllCombustibles());

		this.provinciasMap = serviceFactory.forProvincia().findAllProvincias().stream()
				.collect(Collectors.toMap(Provincia::getExtCode, p -> p));

		this.municipiosMap = serviceFactory.forMunicipio().findAllMunicipios().stream()
				.collect(Collectors.toMap(Municipio::getExtCode, m -> m));

		this.preciosCombustibleParser = new PreciosCombustibleParser(combustibles);

	}

	/**
	 * Parsea un @{@link ESParserDTO} de gasolinera y devuelve un objeto EESS
	 * con toda la información, incluyendo relación con Municipio, Provincia y precios de combustibles.
	 *
	 * @param item @{@link ESParserDTO} de la estación.
	 * @param fecha @{@link LocalDate} Fecha de los precios.
	 * @return  @{@link EstacionDeServicio} EESS parseada y persistida si no existía previamente.
	 * @throws IllegalArgumentException si algún campo es inválido según las reglas de negocio.
	 *
	 */
	public EstacionDeServicio parseEESS(ESParserDTO item, LocalDate fecha) {

		// 1. Obtenemos la identificación, que se asocia a nuestro @EstacionDeServicio.extCode
		int extCode = parseId(item);

		// 2. Parseamos relacionados con objetos
		Provincia provincia = parseProvincia(item);
		Municipio municipio = parseMunicipio(item);

		// 3. Los campos básicos se parsean directamente en el constructor

		// 4. Parseamos los enumerados
		Venta venta = parseVenta(item);
		Remision remision = parseRemision(item);
		Margen margen = parseMargen(item);

		// 5. Parseamos las coordenadas
		double longitud = parseLongitud(item);
		double latitud = parseLatitud(item);

		// 6. Parseamos los porcentajes de BioEtanol y Ester Metílico
		double bioEtanol = parseBioEtanol(item);
		double esterMetilico = parseEsterMetilico(item);

		// Creamos la estacion de servicio con los datos que tenemos:
		EstacionDeServicio eess;
		eess = new EstacionDeServicio(extCode, // Al id le meto extCode xq no vale vació
									  extCode, parseRotulo(item), parseHorario(item),
									  parseDireccion(item), parseLocalidad(item),
									  parseCodigoPostal(item), municipio, provincia,
									  latitud, longitud, margen, remision, venta,
									  bioEtanol, esterMetilico);

		// 8. Parseamos los precios de combustibles y su disponibilidad
		this.parsePrecios(item.getPrecios(), fecha, eess);

		return eess;
	}

	/**
	 * Parsea y acopla los precios de combustibles a una EESS dada.
	 *
	 * @param item  PreciosCombustibleParserDTO de la estación
	 * @param fecha Fecha de los precios
	 * @param eess  Estación de servicio a la que se le acoplan los precios
	 */
	public void parsePrecios(PreciosCombustibleParserDTO item, LocalDate fecha,
							 EstacionDeServicio eess) {
		Set<PrecioCombustible> prComb;
		// Parseo los precios de combustibles
		prComb = preciosCombustibleParser.parsePrecioCombustibleEESS(item, eess, fecha);
		if (prComb == null || prComb.isEmpty()) return;

		// Acoplo los precios a la EESS
		for (PrecioCombustible objPrecio : prComb)
			eess.addPrecioCombustible(objPrecio.getPrecio(), objPrecio.getCombustible(),
									  fecha);
		// Defino los Combustibles disponibles de la EESS
		for (PrecioCombustible objPrecio : prComb)
			eess.addCombustibleDisponible(objPrecio.getCombustible());

		//parseLog.info("La EESS id {} ha generado {} nuevos precios asociados.",
		//			  eess.getId(), eess.getPreciosCombustibles().size());
	}


	// ==============================
	// MÉTODOS PRIVADOS PARA CADA CAMPO
	// ==============================

	//ID

	private int parseId(ESParserDTO item) {
		int value = item.getIdeess();
		if (value <= 0) {
			throw new IllegalArgumentException("ID EESS inválido: " + value);
		}
		return value;
	}

	// Datos básicos


	private String parseRotulo(ESParserDTO item) {
		return item.getRotulo();
	}

	private String parseHorario(ESParserDTO item) {
		return item.getHorario();
	}

	private String parseDireccion(ESParserDTO item) {
		return item.getDireccion();
	}

	private String parseLocalidad(ESParserDTO item) {
		return item.getLocalidad();
	}

	private int parseCodigoPostal(ESParserDTO item) {
		int cp = item.getCp();
		try {
			if (cp < 1000 || cp > 52999)
				throw new IllegalArgumentException("Código postal fuera de rango: " + cp);
			return cp;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Código postal no válido: " + cp, e);
		}
	}

	// Objetos

	private Provincia parseProvincia(ESParserDTO item) {
		int extIdProvincia = item.getIdProvincia();
		if (extIdProvincia < 0 || extIdProvincia > Short.MAX_VALUE) {
			throw new IllegalArgumentException(
					"PK de la Provincia en la eess inválido: " + extIdProvincia);
		}
		if (!provinciasMap.containsKey(Integer.valueOf(extIdProvincia).shortValue()))
			throw new IllegalArgumentException(
					"Provincia no encontrada: " + extIdProvincia);
		return provinciasMap.get(Integer.valueOf(extIdProvincia).shortValue());
	}

	private Municipio parseMunicipio(ESParserDTO item) {
		int extIdMunicipio = item.getIdMunicipio();
		if (extIdMunicipio < 0 || extIdMunicipio > Short.MAX_VALUE) {
			throw new IllegalArgumentException(
					"PK de la Municipio en la eess inválido: " + extIdMunicipio);
		}
		if (!municipiosMap.containsKey(Integer.valueOf(extIdMunicipio).shortValue()))
			throw new IllegalArgumentException(
					"Municipio no encontrada: " + extIdMunicipio);
		return municipiosMap.get(Integer.valueOf(extIdMunicipio).shortValue());
	}

	// Enums

	private Margen parseMargen(ESParserDTO item) {
		return item.getMargen().getRelacionModelo();
	}

	private Remision parseRemision(ESParserDTO item) {
		return item.getRemision().getRelacionModelo();
	}

	private Venta parseVenta(ESParserDTO item) {
		return item.getTipoVenta().getRelacionModelo();
	}

	// Coordenadas

	private double parseLatitud(ESParserDTO item) {
		String latStr = item.getLatitud().replace(",", ".").trim();
		if (latStr.isEmpty()) throw new IllegalArgumentException("Latitud vacía o nula.");
		double lat = Double.parseDouble(latStr);
		if (lat <= 180 && lat >= -180) return lat;
		throw new IllegalArgumentException("Latitud fuera de rango: " + lat);
	}

	private double parseLongitud(ESParserDTO item) {
		String lonStr = item.getLongitud().replace(",", ".").trim();
		if (lonStr.isEmpty())
			throw new IllegalArgumentException("Longitud vacía o nula.");
		double lon = Double.parseDouble(lonStr);
		if (lon <= 180 && lon >= -180) return lon;
		throw new IllegalArgumentException("Longitud fuera de rango: " + lon);
	}


	// Datos numericos

	private double parseBioEtanol(ESParserDTO item) {
		String bioEtanol = item.getBioEtanol();
		if (bioEtanol == null || bioEtanol.isEmpty()) {
			parseLog.error(
					"Error parseando EESS id: {}. El BioEtanol no se encuentra definido. Se deja valor por defecto 0.",
					parseId(item));
			return 0;
		}
		try {
			double x = Double.parseDouble(bioEtanol.replace(",", "."));
			if (x < 0 || x > 100) throw new IllegalArgumentException(
					"Porcentaje de BioEtanol fuera de rango (0-100): " + bioEtanol);
			return x;
		} catch (Exception e) {
			parseLog.error("Error inesperado parseando el BioEtanol: {} . Dato: {}",
						   e.getMessage(), bioEtanol, e);
			return 0;
		}
	}

	private double parseEsterMetilico(ESParserDTO item) {
		String esterMetilico = item.getEsterMetilico();
		if (esterMetilico == null || esterMetilico.isEmpty()) {
			parseLog.error(
					"Error parseando EESS id: {}. El Ester Metílico no se encuentra definido. Se deja valor por defecto 0.",
					parseId(item));
			return 0;
		}
		try {
			double x = Double.parseDouble(item.getEsterMetilico().replace(",", "."));
			if (x < 0 || x > 100) throw new IllegalArgumentException(
					"Porcentaje de Ester Metílico fuera de rango (0-100): " + x);
			return x;
		} catch (Exception e) {
			parseLog.error("Error inesperado parseando el Ester Metílico: {} . Dato: {}",
						   e.getMessage(), item.getEsterMetilico(), e);
			return 0;
		}
	}
}
