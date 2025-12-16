package com.inggarciabaldo.carburo.application.service.eess.crud.commands;

import com.inggarciabaldo.carburo.application.Factorias;
import com.inggarciabaldo.carburo.application.model.Combustible;
import com.inggarciabaldo.carburo.application.model.EstacionDeServicio;
import com.inggarciabaldo.carburo.application.model.PrecioCombustible;
import com.inggarciabaldo.carburo.application.persistance.preciocombustible.PrecioCombustibleGateway;
import com.inggarciabaldo.carburo.application.persistance.preciocombustible.PrecioCombustibleGateway.PrecioCombustibleRecord;
import com.inggarciabaldo.carburo.application.service.combustible.crud.EntityAssembler;
import com.inggarciabaldo.carburo.application.service.util.crud.command.Command;
import com.inggarciabaldo.carburo.util.log.Loggers;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class FindAllPrecioCombustibleByEESSCollectionIdsAndFecha
		implements Command<Collection<PrecioCombustible>> {

	private final Map<Integer, EstacionDeServicio> eessIds;
	private final LocalDate fecha;

	public FindAllPrecioCombustibleByEESSCollectionIdsAndFecha(
			Map<Integer, EstacionDeServicio> eessIds, LocalDate fecha) {
		if (eessIds == null) throw new IllegalArgumentException(
				"La colección de las, junto a las Estaciones de Servicio de los combustibles no puede ser nula.");
		this.eessIds = eessIds;

		if (fecha == null)
			throw new IllegalArgumentException("La fecha no puede ser nula.");
		this.fecha = fecha;
	}

	@Override
	public Collection<PrecioCombustible> execute() {
		logInicioProceso();

		if (eessIds.isEmpty()) {
			logColeccionVacia();
			return Collections.emptySet();
		}

		//Primero recupero todos los combustibles del sistema y hago un map por id para su busqueda
		Map<Short, Combustible> mapCombustibles = Factorias.persistence.forCombustible()
				.findAll().stream()
				.collect(Collectors.toMap(comb -> comb.id, EntityAssembler::toEntity));

		// Recuperamos el conjunto de combustibles disponibles del sistema
		PrecioCombustibleGateway combDispGtwy = Factorias.persistence.forPrecioCombustible();
		Collection<PrecioCombustibleRecord> precioCombustibleRecords = combDispGtwy.findByFecha(
				Date.valueOf(fecha));

		Loggers.DB.info("CARGADOS {} Precios-Combustibles de la BD.",
						precioCombustibleRecords.size());

		if (precioCombustibleRecords.isEmpty()) return Collections.emptySet();

		// Filtramos por EESS incluidas en eessIds y convertimos Record -> DTO
		Collection<PrecioCombustible> salida = precioCombustibleRecords.stream()
				.filter(record -> eessIds.containsKey(record.idEESS))
				.map(record -> new PrecioCombustible(eessIds.get(record.idEESS),
													 mapCombustibles.get(
															 record.idCombustible),
													 record.fecha.toLocalDate(),
													 record.precio))
				.collect(Collectors.toSet());

		Loggers.DB.info(
				"ENCONTRADOS {} objetos Precio-Combustible para la fecha {} y el grupo de EESS ({})",
				salida.size(), fecha, eessIds.size());
		return salida;
	}

	/* ======================================================
	 * =============== MÉTODOS PRIVADOS =====================
	 * ======================================================
	 */

	/**
	 * Log de inicio del proceso.
	 */
	private void logInicioProceso() {
		Loggers.DB.info(
				"COMIENZA el proceso de BÚSQUEDA de Precio-Combustible asociados a la EESS indicadas y a la fecha: {}. Total eess: {}.",
				fecha, eessIds.size());
	}

	/**
	 * Log cuando la colección de EESS está vacía.
	 */
	private void logColeccionVacia() {
		Loggers.DB.info(
				"FINALIZA el proceso de BÚSQUEDA de Precio-Combustible. COLECCIÓN de id de EESS VACÍA.");
	}
}
