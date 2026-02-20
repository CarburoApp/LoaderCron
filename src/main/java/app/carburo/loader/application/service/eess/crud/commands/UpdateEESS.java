package app.carburo.loader.application.service.eess.crud.commands;

import app.carburo.loader.application.Factorias;
import app.carburo.loader.application.model.EstacionDeServicio;
import app.carburo.loader.application.persistance.eess.EESSGateway;
import app.carburo.loader.application.persistance.eess.EESSGateway.EESSRecord;
import app.carburo.loader.application.service.eess.crud.EntityAssembler;
import app.carburo.loader.application.service.util.crud.command.Command;
import app.carburo.loader.util.log.Loggers;

public class UpdateEESS implements Command<EstacionDeServicio> {

	private final EstacionDeServicio estacionDeServicio;

	public UpdateEESS(EstacionDeServicio estacionDeServicio) {
		if (estacionDeServicio == null) throw new IllegalArgumentException(
				"La Estaciones de Servicio no puede ser nula.");

		this.estacionDeServicio = estacionDeServicio;
	}

	@Override
	public EstacionDeServicio execute() {
		// Gateways de persistencia
		EESSGateway gateway = Factorias.persistence.forEESS();

		// Transformación de entidades a records
		EESSRecord record = EntityAssembler.toRecord(estacionDeServicio);

		gateway.update(record);

		Loggers.DB.info("ACTUALIZADA la EESS con id {}.", record.id);

		return estacionDeServicio;
	}
}
