package app.carburo.loader.application.parser;

import app.carburo.loader.application.Factorias;
import app.carburo.loader.application.model.*;
import app.carburo.loader.application.model.enums.Margen;
import app.carburo.loader.application.model.enums.Remision;
import app.carburo.loader.application.model.enums.Venta;
import app.carburo.loader.application.service.ServiceFactory;
import app.carburo.loader.scheduler.jobs.DatosDeEjecucion;
import app.carburo.loader.util.log.Loggers;
import app.carburo.utils.spainMitmaHTTP.shared.model.EstacionDeServicioResponseDTO;
import app.carburo.utils.spainMitmaHTTP.shared.model.PreciosCombustibleResponseDTO;
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
	private final DatosDeEjecucion datosDeEjecucion;

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
	public EESSParser(DatosDeEjecucion datos) {
		if (datos == null) throw new IllegalArgumentException(
				"El objeto datos de ejecución no pueden ser nulos.");
		this.datosDeEjecucion = datos;
		ServiceFactory serviceFactory = Factorias.service;

		// Crear los mapas de referencia para el parser
		Set<Combustible> combustibles = new HashSet<>(serviceFactory.forCombustible().findAllCombustibles());

		this.provinciasMap = serviceFactory.forProvincia().findAllProvincias().stream()
				.collect(Collectors.toMap(Provincia::getExtCode, p -> p));

		this.municipiosMap = serviceFactory.forMunicipio().findAllMunicipios().stream()
				.collect(Collectors.toMap(Municipio::getExtCode, m -> m));

		this.preciosCombustibleParser = new PreciosCombustibleParser(combustibles);

	}

	/**
	 * Parsea un {@link EstacionDeServicioResponseDTO} de gasolinera y devuelve un objeto EESS
	 * con toda la información, incluyendo relación con Municipio, Provincia y precios de combustibles.
	 *
	 * @param item {@link EstacionDeServicioResponseDTO} de la estación.
	 * @param fecha {@link LocalDate} Fecha de los precios.
	 * @return  {@link EstacionDeServicio} EESS parseada y persistida si no existía previamente.
	 * @throws IllegalArgumentException si algún campo es inválido según las reglas de negocio.
	 *
	 */
	public EstacionDeServicio parseEESS(EstacionDeServicioResponseDTO item, LocalDate fecha) {

		// 1. Obtenemos la identificación, que se asocia a nuestro @EstacionDeServicio.extCode
		int extCode = item.getIdeess(); // Comprobación de validez del extCode en el setter del constructor de EESS.

		// 2. Parseamos relacionados con objetos
		Provincia provincia = parseProvincia(item);
		Municipio municipio = parseMunicipio(item);

		// 3. Los campos básicos se parsean directamente en el constructor

		// 4. Parseamos los enumerados
		Venta venta = parseVentaDTO(item);
		Remision remision = parseRemisionDTO(item);
		Margen margen = parseMargenDTO(item);

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
	 * @param item  PreciosCombustibleResponseDTO de la estación
	 * @param fecha Fecha de los precios
	 * @param eess  Estación de servicio a la que se le acoplan los precios
	 */
	public void parsePrecios(PreciosCombustibleResponseDTO item, LocalDate fecha,
							 EstacionDeServicio eess) {
		Set<PrecioCombustible> prComb;
		// Parseo los precios de combustibles
		prComb = preciosCombustibleParser.parsePrecioCombustibleEESS(item, eess, fecha);
		if (prComb == null || prComb.isEmpty()) return;
		datosDeEjecucion.setParseoPreciosCorrectos(
				datosDeEjecucion.getParseoPreciosCorrectos() + prComb.size());

		// Acoplo los precios a la EESS
		for (PrecioCombustible objPrecio : prComb)
			eess.addPrecioCombustible(objPrecio.getPrecio(), objPrecio.getCombustible(),
									  fecha);
		// Defino los Combustibles disponibles de la EESS
		for (PrecioCombustible objPrecio : prComb)
			eess.addCombustibleDisponible(objPrecio.getCombustible());

	}


	// ==============================
	// MÉTODOS PRIVADOS PARA CADA CAMPO
	// ==============================

	private String parseRotulo(EstacionDeServicioResponseDTO item) {
		return item.getRotulo();
	}

	private String parseHorario(EstacionDeServicioResponseDTO item) {
		return item.getHorario();
	}

	private String parseDireccion(EstacionDeServicioResponseDTO item) {
		return item.getDireccion();
	}

	private String parseLocalidad(EstacionDeServicioResponseDTO item) {
		return item.getLocalidad();
	}

	private int parseCodigoPostal(EstacionDeServicioResponseDTO item) {
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

	private Provincia parseProvincia(EstacionDeServicioResponseDTO item) {
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

	private Municipio parseMunicipio(EstacionDeServicioResponseDTO item) {
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

	private Margen parseMargenDTO(EstacionDeServicioResponseDTO dto) {
		if (dto == null || dto.getMargen() == null) throw new IllegalArgumentException();

		return switch (dto.getMargen()) {
			case DERECHO -> Margen.DERECHO;
			case IZQUIERDO -> Margen.IZQUIERDO;
			default -> Margen.NO_APLICA;
		};
	}

	private Remision parseRemisionDTO(EstacionDeServicioResponseDTO dto) {
		if (dto == null || dto.getRemision() == null) throw new IllegalArgumentException();

		return switch (dto.getRemision()) {
			case DM -> Remision.DM;
			case OM -> Remision.OM;
			default ->
					throw new IllegalStateException("No se debería de llegar a este punto, el valor de remisión debería ser DM o OM. Valor actual: " + dto.getRemision());
		};
	}

	private Venta parseVentaDTO(EstacionDeServicioResponseDTO dto) {
		if (dto == null || dto.getTipoVenta() == null) throw new IllegalArgumentException();

		return switch (dto.getTipoVenta()) {
			case PUBLICA -> Venta.PUBLICA;
			case RESTRINGIDA -> Venta.RESTRINGIDA;
			default ->
					throw new IllegalStateException("No se debería de llegar a este punto, el valor de venta debería ser publica o privada. Valor actual: " + dto.getTipoVenta());
		};
	}

	// Coordenadas

	private double parseLatitud(EstacionDeServicioResponseDTO item) {
		String latStr = item.getLatitud().replace(",", ".").trim();
		if (latStr.isEmpty()) throw new IllegalArgumentException("Latitud vacía o nula.");
		double lat = Double.parseDouble(latStr);
		if (lat <= 180 && lat >= -180) return lat;
		throw new IllegalArgumentException("Latitud fuera de rango: " + lat);
	}

	private double parseLongitud(EstacionDeServicioResponseDTO item) {
		String lonStr = item.getLongitud().replace(",", ".").trim();
		if (lonStr.isEmpty())
			throw new IllegalArgumentException("Longitud vacía o nula.");
		double lon = Double.parseDouble(lonStr);
		if (lon <= 180 && lon >= -180) return lon;
		throw new IllegalArgumentException("Longitud fuera de rango: " + lon);
	}


	// Datos numericos

	private double parseBioEtanol(EstacionDeServicioResponseDTO item) {
		String bioEtanol = item.getBioEtanol();
		if (bioEtanol == null || bioEtanol.isEmpty()) {
			parseLog.error("Error parseando EESS id: {}. El BioEtanol no se encuentra definido. Se deja valor por defecto 0.",
					item.getIdeess());
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

	private double parseEsterMetilico(EstacionDeServicioResponseDTO item) {
		String esterMetilico = item.getEsterMetilico();
		if (esterMetilico == null || esterMetilico.isEmpty()) {
			parseLog.error(
					"Error parseando EESS id: {}. El Ester Metílico no se encuentra definido. Se deja valor por defecto 0.",
					item.getIdeess());
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
