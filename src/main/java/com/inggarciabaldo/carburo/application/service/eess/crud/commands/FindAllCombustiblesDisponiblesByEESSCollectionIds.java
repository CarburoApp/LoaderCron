package com.inggarciabaldo.carburo.application.service.eess.crud.commands;

import com.inggarciabaldo.carburo.application.Factorias;
import com.inggarciabaldo.carburo.application.persistance.combustibleDisponible.CombustibleDisponibleGateway;
import com.inggarciabaldo.carburo.application.persistance.combustibleDisponible.CombustibleDisponibleGateway.CombustibleDisponibleRecord;
import com.inggarciabaldo.carburo.application.service.util.command.Command;
import com.inggarciabaldo.carburo.application.service.util.dto.CombustibleDisponibleDTO;
import com.inggarciabaldo.carburo.util.log.Loggers;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class FindAllCombustiblesDisponiblesByEESSCollectionIds
		implements Command<Collection<CombustibleDisponibleDTO>> {

	private final Collection<Integer> eessIds;

	/**
	 * El objetivo es obtener todos los objetos Combustible Disponible de la base de datos
	 * que case con las EESS pasadas por parámetro.
	 *
	 * @param eessIds {@link Collection} con los IDs de las EESS que deseamos.
	 */
	public FindAllCombustiblesDisponiblesByEESSCollectionIds(
			Collection<Integer> eessIds) {
		if (eessIds == null)
			throw new IllegalArgumentException("El id de la EESS no puede ser negativo");
		this.eessIds = eessIds;
	}


	@Override
	public Collection<CombustibleDisponibleDTO> execute() {
		// Si la lista de ids está vacía, no se quiere ningún combustible disponible
		if (this.eessIds.isEmpty()) {
			return Collections.emptySet();
		}

		// Recuperamos el conjunto de combustibles disponibles del sistema
		CombustibleDisponibleGateway combDispGtwy = Factorias.persistence.forCombustibleDisponible();
		Collection<CombustibleDisponibleRecord> combDispRecord = combDispGtwy.findAll();

		Loggers.DB.info("CARGADOS {} combustibles-disponibles de la BD.",
						combDispRecord.size());

		if (combDispRecord.isEmpty()) return Collections.emptySet();

		// Filtramos por EESS incluidas en eessIds y convertimos Record -> DTO
		Collection<CombustibleDisponibleDTO> salida;
		salida = combDispRecord.stream().filter(record -> eessIds.contains(record.idEESS))
				.map(record -> new CombustibleDisponibleDTO(record.idCombustible,
															record.idEESS))
				.collect(Collectors.toSet());
		Loggers.DB.info(
				"ENCONTRADAS {} objetos Combustible-Disponible para el grupo de EESS ({})",
				salida.size(), eessIds.size());
		return salida;
	}
}
