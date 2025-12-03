package com.inggarciabaldo.carburo.application.rest.parser;

import com.inggarciabaldo.carburo.application.model.*;
import com.inggarciabaldo.carburo.application.model.enums.Margen;
import com.inggarciabaldo.carburo.application.model.enums.Remision;
import com.inggarciabaldo.carburo.application.model.enums.Venta;
import com.inggarciabaldo.carburo.util.log.Loggers;
import com.inggarciabaldo.carburo.util.properties.PropertyLoader;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

// TODO
public class EESSParser {

	// Logger para operaciones de parseo
	private static final Logger parseLog = Loggers.PARSE;
	// Propiedades con las claves de JSON
	private static final Properties claves = PropertyLoader.getInstance()
			.getJsonKeysProperties();

	//Repositorios
	//	private final EESSRepository repoEESS = RepositoryFactory.getEESSRepository();
	//	private final CombustibleRepository repoCombustible = RepositoryFactory.getCombustibleRepository();
	//	private final PrecioCombustibleRepository repoPrecioCombustible = RepositoryFactory.getPrecioCombustibleRepository();

	// ==============================
	// Campos de cada estación (excluyendo tipos de combustible)
	// ==============================
	public static final String IDEESS = claves.getProperty("ideess");
	public static final String ROTULO = claves.getProperty("rotulo");
	public static final String DIRECCION = claves.getProperty("direccion");
	public static final String HORARIO = claves.getProperty("horario");
	public static final String CP = claves.getProperty("cp");
	public static final String LOCALIDAD = claves.getProperty("localidad");
	public static final String IDPROVINCIA = claves.getProperty("idprovincia");
	public static final String IDMUNICIPIO = claves.getProperty("idmunicipio");
	public static final String LATITUD = claves.getProperty("latitud");
	public static final String LONGITUD = claves.getProperty("longitud");
	public static final String MARGEN = claves.getProperty("margen");
	public static final String REMISION = claves.getProperty("remision");
	public static final String TIPO_VENTA = claves.getProperty("tipo.venta");
	public static final String BIOETANOL = claves.getProperty("bioetanol");
	public static final String ESTER_METILICO = claves.getProperty("ester.metilico");

	// ==============================
	// Prefijo genérico para todos los precios
	// ==============================
	public static final String PRECIO_PREFIX = claves.getProperty("precio.prefix");


	// Mapas de referencia para evitar consultas repetidas
	private final Map<BigDecimal, Provincia> provinciasMap;
	private final Map<BigDecimal, Municipio> municipiosMap;
	private final Map<String, Combustible> combustiblesMap;
	private final Set<BigDecimal> eessExistentes;

	/**
	 * Constructor que recibe los mapas de referencia ya cargados.
	 *
	 * @param provinciasMap   Mapa de provincias por extCode
	 * @param municipiosMap   Mapa de municipios por extCode
	 * @param combustiblesMap Mapa de combustibles por denominación
	 */
	public EESSParser(Map<BigDecimal, Provincia> provinciasMap,
					  Map<BigDecimal, Municipio> municipiosMap,
					  Map<String, Combustible> combustiblesMap,
					  Set<BigDecimal> eessExistentes) {
		this.provinciasMap   = provinciasMap;
		this.municipiosMap   = municipiosMap;
		this.combustiblesMap = combustiblesMap;
		this.eessExistentes  = eessExistentes;
	}

	/**
	 * Parsea un JSONObject de gasolinera y devuelve un objeto EESS
	 * con toda la información, incluyendo relación con Municipio, Provincia y precios de combustibles.
	 *
	 * @param item JSONObject de la estación
	 * @param date Fecha de los precios
	 * @return EESS parseada y persistida si no existía previamente
	 * @throws IllegalArgumentException si algún campo obligatorio es inválido
	 */
	public ES parse(JSONObject item, LocalDate date) {
		return parse(item, date, null);
	}

	/**
	 * Parsea un JSONObject de gasolinera y devuelve un objeto EESS
	 * con toda la información, incluyendo relación con Municipio, Provincia y precios de combustibles.
	 *
	 * @param item        JSONObject de la estación
	 * @param fecha       Fecha de los precios
	 * @param combustible Combustible específico a parsear (opcional). Null = todos.
	 * @return EESS parseada y persistida si no existía previamente. Si ya está definida, devuelve null.
	 * @throws IllegalArgumentException si algún campo obligatorio es inválido
	 */
	public ES parse(JSONObject item, LocalDate fecha, Combustible combustible) {
		// 1. Parseamos campos básicos y relaciones
		BigDecimal extCode = parseExtCode(item);
		Provincia provincia = parseProvincia(item);
		Municipio municipio = parseMunicipio(item);

		// 2. Buscamos EESS existente
		if (eessExistentes.contains(extCode)) return null;
		else {
			// Crear nueva EESS si no existe
			ES ES = new ES(extCode, parseRotulo(item), parseHorario(item),
						   parseDireccion(item), parseCodigoPostal(item),
						   parseLocalidad(item), parseLatitud(item), parseLongitud(item),
						   parseMargen(item), parseRemision(item), parseVenta(item),
						   municipio, provincia);
			ES.setX100BioEtanol(parseBioEtanol(item));
			ES.setX100EsterMetilico(parseEsterMetilico(item));
			return ES;
		}
		// 3. Parseamos precios de combustibles
		//parsePrecios(item, eess, fecha, combustible);
	}

	/**
	 * Parsea los precios de combustibles de una EESS desde un JSONObject.
	 * Si combustible es null, se procesan todos los combustibles.
	 * Si combustible != null, solo se parsea ese combustible usando la clave PRECIO_PREFIX.
	 *
	 * @param item        JSONObject de la estación
	 * @param ES          EESS asociada
	 * @param fecha       Fecha de los precios
	 * @param combustible Combustible específico a parsear (opcional). Null = todos.
	 */
	private void parsePrecios(JSONObject item, ES ES, LocalDate fecha,
							  Combustible combustible) {
		if (combustible == null) {
			parsePreciosTodosCombustibles(item, ES, fecha);
		} else {
			parsePrecioCombustible(item, ES, fecha, combustible);
		}
	}

	/**
	 * Itera todos los combustibles y parsea sus precios.
	 */
	private void parsePreciosTodosCombustibles(JSONObject item, ES ES, LocalDate fecha) {
		List<Combustible> combustibles = null;//= repoCombustible.findAll();
		for (Combustible c : combustibles) {
			BigDecimal precio = parsearPrecio(item, obtenerClaveCombustibleJson(c), c);
			if (precio != null) {
				persistirPrecio(ES, c, fecha, precio);
				actualizarDisponibilidadCombustible(ES, c);
			}
		}
	}

	/**
	 * Parsea solo un combustible concreto usando la clave PRECIO_PREFIX.
	 */
	private void parsePrecioCombustible(JSONObject item, ES ES, LocalDate fecha,
										Combustible c) {
		BigDecimal precio = parsearPrecio(item, PRECIO_PREFIX, c);
		if (precio != null) {
			persistirPrecio(ES, c, fecha, precio);
			actualizarDisponibilidadCombustible(ES, c);
		}
	}

	/**
	 * Devuelve la clave JSON que se usará para extraer el precio.
	 */
	private String obtenerClaveCombustibleJson(Combustible combustible) {
		return PRECIO_PREFIX + " " + combustible.getDenominacion();
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
	private void persistirPrecio(ES ES, Combustible combustible, LocalDate fecha,
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
	private void actualizarDisponibilidadCombustible(ES ES, Combustible combustible) {
		if (!ES.getCombustiblesDisponibles().contains(combustible)) {
			ES.getCombustiblesDisponibles().add(combustible);
			//repoEESS.update(ES);
		}
	}


	// ==============================
	// MÉTODOS PRIVADOS PARA CADA CAMPO
	// ==============================

	private BigDecimal parseExtCode(JSONObject item) {
		BigDecimal value = item.optBigDecimal(IDEESS, BigDecimal.valueOf(-1));
		if (value.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("ID EESS inválido: " + value);
		}
		return value;
	}


	private String parseRotulo(JSONObject item) {
		return item.optString(ROTULO).trim();
	}

	private String parseHorario(JSONObject item) {
		return item.optString(HORARIO).trim();
	}

	private String parseDireccion(JSONObject item) {
		return item.optString(DIRECCION).trim();
	}

	private String parseLocalidad(JSONObject item) {
		return item.optString(LOCALIDAD).trim();
	}

	private BigDecimal parseCodigoPostal(JSONObject item) {
		String cpStr = item.optString(CP, "").trim();
		if (cpStr.isEmpty())
			throw new IllegalArgumentException("Código postal vacío o nulo.");
		try {
			int cp = Integer.parseInt(cpStr);
			if (cp < 1000 || cp > 52999)
				throw new IllegalArgumentException("Código postal fuera de rango: " + cp);
			return BigDecimal.valueOf(cp);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Código postal no válido: " + cpStr, e);
		}
	}

	private Provincia parseProvincia(JSONObject item) {
		BigDecimal extCodeProvincia = item.optBigDecimal(IDPROVINCIA,
														 BigDecimal.valueOf(-1));
		if (extCodeProvincia.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException(
					"ExtCode Provincia inválido: " + extCodeProvincia);
		}
		if (!provinciasMap.containsKey(extCodeProvincia))
			throw new IllegalArgumentException(
					"Provincia no encontrada: " + extCodeProvincia);
		return provinciasMap.get(extCodeProvincia);
	}

	private Municipio parseMunicipio(JSONObject item) {
		BigDecimal extCodeMunicipio = item.optBigDecimal(IDMUNICIPIO,
														 BigDecimal.valueOf(-1));
		if (extCodeMunicipio.compareTo(BigDecimal.ZERO) <= 0)
			throw new IllegalArgumentException(
					"ExtCode Municipio inválido: " + extCodeMunicipio);
		// Buscamos el municipio en la base de datos
		if (!municipiosMap.containsKey(extCodeMunicipio))
			throw new IllegalArgumentException(
					"Municipio no encontrado: " + extCodeMunicipio);
		return municipiosMap.get(extCodeMunicipio);
	}


	private BigDecimal parseLatitud(JSONObject item) {
		String latStr = item.optString(LATITUD, "").replace(",", ".").trim();
		if (latStr.isEmpty()) throw new IllegalArgumentException("Latitud vacía o nula.");
		BigDecimal lat = new BigDecimal(latStr);
		if (lat.compareTo(BigDecimal.valueOf(-90)) < 0 ||
				lat.compareTo(BigDecimal.valueOf(90)) > 0)
			throw new IllegalArgumentException("Latitud fuera de rango: " + lat);
		return lat;
	}

	private BigDecimal parseLongitud(JSONObject item) {
		String lonStr = item.optString(LONGITUD, "").replace(",", ".").trim();
		if (lonStr.isEmpty())
			throw new IllegalArgumentException("Longitud vacía o nula.");
		BigDecimal lon = new BigDecimal(lonStr);
		if (lon.compareTo(BigDecimal.valueOf(-180)) < 0 ||
				lon.compareTo(BigDecimal.valueOf(180)) > 0)
			throw new IllegalArgumentException("Longitud fuera de rango: " + lon);
		return lon;
	}

	private Margen parseMargen(JSONObject item) {
		String code = item.optString(MARGEN).trim();
		return Margen.fromCode(code);
	}

	private Remision parseRemision(JSONObject item) {
		String code = item.optString(REMISION).trim();
		return Remision.fromCode(code);
	}

	private Venta parseVenta(JSONObject item) {
		String code = item.optString(TIPO_VENTA).trim();
		return Venta.fromCode(code);
	}

	private BigDecimal parseBioEtanol(JSONObject item) {
		try {
			return new BigDecimal(item.optString(BIOETANOL).replace(",", "."));
		} catch (Exception e) {
			return BigDecimal.ZERO;
		}
	}

	private BigDecimal parseEsterMetilico(JSONObject item) {
		try {
			return new BigDecimal(item.optString(ESTER_METILICO).replace(",", "."));
		} catch (Exception e) {
			return BigDecimal.ZERO;
		}
	}
}
