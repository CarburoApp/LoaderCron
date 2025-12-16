package com.inggarciabaldo.carburo.application.service.eess.crud.commands;

import com.inggarciabaldo.carburo.application.Factorias;
import com.inggarciabaldo.carburo.application.persistance.combustibleDisponible.CombustibleDisponibleGateway;
import com.inggarciabaldo.carburo.application.persistance.combustibleDisponible.CombustibleDisponibleGateway.CombustibleDisponibleRecord;
import com.inggarciabaldo.carburo.application.service.util.command.Command;
import com.inggarciabaldo.carburo.application.service.util.dto.CombustibleDisponibleDTO;
import com.inggarciabaldo.carburo.util.log.Loggers;

import java.util.ArrayList;
import java.util.Collection;

public class AddAllCombustiblesDisponibles implements Command<Integer> {

	private final Collection<CombustibleDisponibleDTO> combustiblesDisponibles;

	public AddAllCombustiblesDisponibles(
			Collection<CombustibleDisponibleDTO> combustiblesDisponibles) {
		if (combustiblesDisponibles == null) throw new IllegalArgumentException(
				"La colección de Combustible-Disponible no puede ser nula.");
		this.combustiblesDisponibles = combustiblesDisponibles;
	}

	@Override
	public Integer execute() {
		logInicioProceso();

		if (combustiblesDisponibles.isEmpty()) {
			logColeccionVacia();
			return 0;
		}

		// Gateways de persistencia
		CombustibleDisponibleGateway gateway = Factorias.persistence.forCombustibleDisponible();

		// Transformación de entidades a records
		Collection<CombustibleDisponibleRecord> records = new ArrayList<>();
		for (CombustibleDisponibleDTO disponibleDTO : combustiblesDisponibles) {
			CombustibleDisponibleRecord record = new CombustibleDisponibleRecord();
			record.idEESS        = disponibleDTO.idEESS;
			record.idCombustible = disponibleDTO.idCombustible;
			records.add(record);
		}

		Loggers.DB.info(
				"Transformadas {} entidades Precio-Combustible a {} records Precio-Combustible.",
				combustiblesDisponibles.size(), records.size());

		Loggers.DB.info(
				"COMIENZO de INSERCIÓN MASIVA de Combustible-Disponible. Pendientes: {}.",
				records.size());

		long inicio = System.currentTimeMillis();
		int insertadas = gateway.addAll(records);

		Loggers.DB.info("INSERTADOS {} de {} Combustible-Disponible en {} ms.",
						insertadas, records.size(), System.currentTimeMillis() - inicio);

		return insertadas;
	}

	/* ======================================================
	 * =============== MÉTODOS PRIVADOS =====================
	 * ====================================================== */

	/**
	 * Log de inicio del proceso.
	 */
	private void logInicioProceso() {
		Loggers.DB.info(
				"COMIENZA el proceso de INSERCIÓN MASIVA de Combustible-Disponible con disponibilidad y precios. Total Combustible-Disponibles: {}.",
				combustiblesDisponibles.size());
	}

	/**
	 * Log cuando la colección de EESS está vacía.
	 */
	private void logColeccionVacia() {
		Loggers.DB.info(
				"FINALIZA el proceso de INSERCIÓN MASIVA de Combustible-Disponible. COLECCIÓN VACÍA.");
	}
}
