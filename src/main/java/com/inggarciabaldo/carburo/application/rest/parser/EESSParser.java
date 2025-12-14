package com.inggarciabaldo.carburo.application.rest.parser;

import com.inggarciabaldo.carburo.application.Factorias;
import com.inggarciabaldo.carburo.application.model.Combustible;
import com.inggarciabaldo.carburo.application.model.EstacionDeServicio;
import com.inggarciabaldo.carburo.application.model.Municipio;
import com.inggarciabaldo.carburo.application.model.Provincia;
import com.inggarciabaldo.carburo.application.model.enums.Margen;
import com.inggarciabaldo.carburo.application.model.enums.Remision;
import com.inggarciabaldo.carburo.application.model.enums.Venta;
import com.inggarciabaldo.carburo.application.rest.dto.ESParserDTO;
import com.inggarciabaldo.carburo.application.service.ServiceFactory;
import com.inggarciabaldo.carburo.util.log.Loggers;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Parser específico para estaciones de servicio (EESS).
 */
public class EESSParser {

	// Logger para operaciones de parseo
	private static final Logger parseLog = Loggers.PARSE;


	// Mapas de referencia para evitar consultas repetidas
	private final Map<Short, Provincia> provinciasMap;
	private final Map<Short, Municipio> municipiosMap;
	private final Map<String, Combustible> combustiblesMap;
	private final Map<Integer, EstacionDeServicio> eessExistentes;


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
		this.combustiblesMap = serviceFactory.forCombustible().findAllCombustibles()
				.stream().collect(Collectors.toMap(Combustible::getDenominacion, c -> c));

		this.provinciasMap = serviceFactory.forProvincia().findAllProvincias().stream()
				.collect(Collectors.toMap(Provincia::getExtCode, p -> p));

		this.municipiosMap = serviceFactory.forMunicipio().findAllMunicipios().stream()
				.collect(Collectors.toMap(Municipio::getExtCode, m -> m));

		this.eessExistentes = serviceFactory.forEESS().findAllEESS().stream()
				.collect(Collectors.toMap(EstacionDeServicio::getExtCode, p -> p));
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


		// 3. Parseamos precios de combustibles
		//parsePrecios(item, eess, fecha, combustible);

		return eess;
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


	/**
	 * ==============================
	 *  FIN
	 *  ==============================
	 */




	/**
	 * Parsea los precios de combustibles de una EESS desde un JSONObject.
	 * Si combustible es null, se procesan todos los combustibles.
	 * Si combustible != null, solo se parsea ese combustible usando la clave PRECIO_PREFIX.
	 *
	 * @param item        JSONObject de la estación
	 * @param estacionDeServicio          EESS asociada
	 * @param fecha       Fecha de los precios
	 * @param combustible Combustible específico a parsear (opcional). Null = todos.
	 */
	private void parsePrecios(JSONObject item, EstacionDeServicio estacionDeServicio,
							  LocalDate fecha,
							  Combustible combustible) {
		if (combustible == null) {
			parsePreciosTodosCombustibles(item, estacionDeServicio, fecha);
		} else {
			parsePrecioCombustible(item, estacionDeServicio, fecha, combustible);
		}
	}

	/**
	 * Itera todos los combustibles y parsea sus precios.
	 */
	private void parsePreciosTodosCombustibles(JSONObject item,
											   EstacionDeServicio estacionDeServicio,
											   LocalDate fecha) {
		List<Combustible> combustibles = null;//= repoCombustible.findAll();
		for (Combustible c : combustibles) {
			BigDecimal precio = parsearPrecio(item, "", c);
			if (precio != null) {
				persistirPrecio(estacionDeServicio, c, fecha, precio);
				actualizarDisponibilidadCombustible(estacionDeServicio, c);
			}
		}
	}

	/**
	 * Parsea solo un combustible concreto usando la clave PRECIO_PREFIX.
	 */
	private void parsePrecioCombustible(JSONObject item,
										EstacionDeServicio estacionDeServicio,
										LocalDate fecha,
										Combustible c) {
		BigDecimal precio = parsearPrecio(item, "", c);
		if (precio != null) {
			persistirPrecio(estacionDeServicio, c, fecha, precio);
			actualizarDisponibilidadCombustible(estacionDeServicio, c);
		}
	}

	/**
	 * Parsea el precio de un combustible a BigDecimal.
	 * Devuelve null si el precio está vacío o inválido.
	 */
	private BigDecimal parsearPrecio(JSONObject item, String jsonKey,
									 Combustible combustible) {
		String precioStr = item.optString(jsonKey, "").replace(",", ".").trim();
		if (precioStr.isEmpty()) return null;
		try {
			return new BigDecimal(precioStr);
		} catch (NumberFormatException e) {
			parseLog.error("Precio inválido para {}: {}", combustible.getDenominacion(),
						   precioStr, e);
			return null;
		}
	}

	/**
	 * Persiste o actualiza un precio en la base de datos.
	 */
	private void persistirPrecio(EstacionDeServicio estacionDeServicio,
								 Combustible combustible, LocalDate fecha,
								 BigDecimal precio) {
		//		PrecioCombustible existente = repoPrecioCombustible.findByEESSAndCombustibleAndFecha(
		//				ES, combustible, fecha);
		//
		//		if (existente != null) {
		//			if (existente.getPrecio().compareTo(precio) != 0) {
		//				existente.setPrecio(precio);
		//				repoPrecioCombustible.update(existente);
		//			}
		//		} else {
		//			// Aseguramos que las entidades relacionadas están gestionadas
		//			EntityManager em = JPAUtil.getEntityManager();
		//			ES managedES = em.getReference(ES.class, ES.getId());
		//			Combustible managedCombustible = em.getReference(Combustible.class,
		//															 combustible.getId());
		//
		//			PrecioCombustible nuevo = new PrecioCombustible(managedES, managedCombustible,
		//															fecha, precio);
		//			repoPrecioCombustible.save(nuevo); // persist ahora es seguro
		//		}
	}


	/**
	 * Actualiza la lista de combustibles disponibles de la EESS.
	 */
	private void actualizarDisponibilidadCombustible(
			EstacionDeServicio estacionDeServicio, Combustible combustible) {
		if (!estacionDeServicio.getCombustiblesDisponibles().contains(combustible)) {
			estacionDeServicio.getCombustiblesDisponibles().add(combustible);
			//repoEESS.update(ES);
		}
	}


}
