package app.carburo.loader.application.service.eess.crud.commands;

import app.carburo.loader.application.Factorias;
import app.carburo.loader.application.model.PrecioCombustible;
import app.carburo.loader.application.persistance.preciocombustible.PrecioCombustibleGateway;
import app.carburo.loader.application.persistance.preciocombustible.PrecioCombustibleGateway.PrecioCombustibleRecord;
import app.carburo.loader.application.service.eess.crud.EntityAssembler;
import app.carburo.loader.application.service.util.crud.command.Command;
import app.carburo.loader.util.log.Loggers;

import java.util.ArrayList;
import java.util.Collection;

public class AddAllPrecioCombustibles implements Command<Integer> {

	private final Collection<PrecioCombustible> precioCombustibles;

	public AddAllPrecioCombustibles(Collection<PrecioCombustible> precioCombustibles) {
		if (precioCombustibles == null) throw new IllegalArgumentException(
				"La colección de precios de combustibles no puede ser nula.");

		this.precioCombustibles = precioCombustibles;
	}

	@Override
	public Integer execute() {
		logInicioProceso();

		if (precioCombustibles.isEmpty()) {
			logColeccionVacia();
			return 0;
		}

		// Gateways de persistencia
		PrecioCombustibleGateway gateway = Factorias.persistence.forPrecioCombustible();

		// Transformación de entidades a records
		Collection<PrecioCombustibleRecord> records = new ArrayList<>();
		for (PrecioCombustible precio : precioCombustibles)
			records.add(EntityAssembler.toPrecioCombustibleRecord(precio));

		Loggers.DB.info(
				"Transformadas {} entidades Precio-Combustible a {} records Precio-Combustible.",
				precioCombustibles.size(), records.size());

		Loggers.DB.info(
				"COMIENZO de INSERCIÓN MASIVA de Precio-Combustible. Pendientes: {}.",
				records.size());

		long inicio = System.currentTimeMillis();
		int insertadas = gateway.addAll(records);

		Loggers.DB.info("INSERTADOS {} de {} Precio-Combustible en {} ms.", insertadas,
						records.size(), System.currentTimeMillis() - inicio);

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
				"COMIENZA el proceso de INSERCIÓN MASIVA de Precio-Combustible con disponibilidad y precios. Total Precios-Combustibles: {}.",
				precioCombustibles.size());
	}

	/**
	 * Log cuando la colección de EESS está vacía.
	 */
	private void logColeccionVacia() {
		Loggers.DB.info(
				"FINALIZA el proceso de INSERCIÓN MASIVA de Precio-Combustible. COLECCIÓN VACÍA.");
	}
}
