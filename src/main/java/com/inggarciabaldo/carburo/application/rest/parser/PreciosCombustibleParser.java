package com.inggarciabaldo.carburo.application.rest.parser;

import com.inggarciabaldo.carburo.application.model.Combustible;
import com.inggarciabaldo.carburo.application.model.EstacionDeServicio;
import com.inggarciabaldo.carburo.application.model.PrecioCombustible;
import com.inggarciabaldo.carburo.application.rest.dto.PreciosCombustibleParserDTO;
import com.inggarciabaldo.carburo.config.parser.api.EstractorPreciosPorCodigo;
import com.inggarciabaldo.carburo.util.log.Loggers;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Parser específico para los precios de una estación de servicio (EESS).
 */
public class PreciosCombustibleParser {

	// Logger para operaciones de parseo
	private static final Logger parseLog = Loggers.PARSE;

	private final Set<Combustible> combustibles;

	/**
	 * Constructor del parser precios de EESS.
	 * <p>
	 * Se encarga de cargar los datos iniciales de referencia necesarios para el parseo.
	 * Estos datos los tomará de la BD o de la Caché según corresponda.
	 * <p>
	 * Se recomienda encarecidamente usar la caché para optimizar el rendimiento.
	 *
	 * @param combustibles Conjunto de combustibles disponibles en la aplicación.
	 */
	public PreciosCombustibleParser(Set<Combustible> combustibles) {
		this.combustibles = combustibles;
	}

	/**
	 * Parsea los precios de combustibles de una EESS desde un DTO concreto.
	 *
	 * @param item  PreciosCarburanteParserDTO de la estación
	 * @param fecha Fecha de los precios
	 * @return Set<PrecioCombustible> Conjunto de objetos @{@link PrecioCombustible} parseados.
	 */
	public Set<PrecioCombustible> parsePrecioCombustibleEESS(
			PreciosCombustibleParserDTO item, EstacionDeServicio estacion,
			LocalDate fecha) {
		// Defino el almacenamiento de salida
		Set<PrecioCombustible> salidaPrecios = new HashSet<>();

		PrecioCombustible precioCombustible;
		for (Combustible c : combustibles) {
			Function<PreciosCombustibleParserDTO, String> extractor = EstractorPreciosPorCodigo.PRECIO_EXTRACTORS.get(
					c.getCodigo());
			// No hay precio para este combustible en el DTO
			if (extractor == null) continue;

			double precio = parsePrecio(extractor.apply(item));

			// Precio no disponible, se omite
			if (precio == -1) continue;

			try {
				precioCombustible = new PrecioCombustible(estacion, c, fecha, precio);
			} catch (IllegalArgumentException e) {
				parseLog.error("No se ha podido parsear el precio del combustible {} " +
									   "para la EESS id {} en la fecha {}. Se omite este precio.",
							   c.getDenominacion(), estacion.getId(), fecha);
				continue;
			}
			salidaPrecios.add(precioCombustible);
		}

		return salidaPrecios;
	}

	/**
	 * Parsea el precio de un combustible específico desde el DTO.
	 * La denominación del combustible se usa para identificar el campo correcto.
	 * Lanza IllegalArgumentException si el precio no es válido.
	 *
	 * @param item        DTO de precios
	 * @param estacion    Estación de servicio
	 * @param fecha       Fecha del precio
	 * @param combustible Combustible a parsear
	 * @return PrecioCombustible creado a partir del parseado
	 */
	public PrecioCombustible parsePrecioCombustibleEESS(PreciosCombustibleParserDTO item,
														EstacionDeServicio estacion,
														LocalDate fecha,
														Combustible combustible) {
		// Seleccionado el combustible en el filtro, la api lo entrega en "Precio":
		double precio = parsePrecio(item.getPrecio());
		return new PrecioCombustible(estacion, combustible, fecha, precio);
	}


	// ==============================
	// MÉTODOS PRIVADOS PARA CADA CAMPO
	// ==============================


	/**
	 * Parsea el precio de combustible desde el String proporcionado.
	 * Lanza IllegalArgumentException si el precio no es válido.
	 *
	 * @param precioStr String con el precio a parsear
	 * @return double con el precio parseado
	 */
	private double parsePrecio(String precioStr) {
		if (precioStr == null || precioStr.isEmpty()) {
			return -1;
		}
		precioStr = precioStr.replace(",", ".").trim();
		if (precioStr.isEmpty()) return -1;
		double precio = Double.parseDouble(precioStr);
		if (precio < 0 || precio > 100) throw new IllegalArgumentException(
				"Precio de combustible fuera de rango (0-100): " + precio);
		return precio;
	}
}
