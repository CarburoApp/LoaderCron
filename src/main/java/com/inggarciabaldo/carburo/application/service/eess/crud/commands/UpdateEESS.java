package com.inggarciabaldo.carburo.application.service.eess.crud.commands;

import com.inggarciabaldo.carburo.application.Factorias;
import com.inggarciabaldo.carburo.application.model.EstacionDeServicio;
import com.inggarciabaldo.carburo.application.persistance.eess.EESSGateway;
import com.inggarciabaldo.carburo.application.persistance.eess.EESSGateway.EESSRecord;
import com.inggarciabaldo.carburo.application.service.eess.crud.EntityAssembler;
import com.inggarciabaldo.carburo.application.service.util.crud.command.Command;
import com.inggarciabaldo.carburo.util.log.Loggers;

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
