package com.inggarciabaldo.carburo.application.service.eess.crud.commands;

import com.inggarciabaldo.carburo.application.Factorias;
import com.inggarciabaldo.carburo.application.model.EstacionDeServicio;
import com.inggarciabaldo.carburo.application.persistance.eess.EESSGateway;
import com.inggarciabaldo.carburo.application.persistance.eess.EESSGateway.EESSRecord;
import com.inggarciabaldo.carburo.application.service.eess.crud.EntityAssembler;
import com.inggarciabaldo.carburo.application.service.util.command.Command;
import com.inggarciabaldo.carburo.util.log.Loggers;

import java.util.Optional;

public class FindEESSById implements Command<Optional<EstacionDeServicio>> {

	private final String id;

	public FindEESSById(String id) {
		if (id != null) this.id = id;
		throw new IllegalArgumentException("El id de la EESS no puede ser nulo");
	}

	@Override
	public Optional<EstacionDeServicio> execute() {
		//Recuperamos el municipio a traves del gateway
		EESSGateway mg = Factorias.persistence.forEESS();
		Optional<EESSRecord> eessRecord = mg.findById(String.valueOf(this.id));
		Loggers.DB.info("Se ha cargado la estación de servicio de la BD con id {}.", id);

		if (eessRecord.isEmpty()) return Optional.empty();

		EstacionDeServicio res = EntityAssembler.toEntity(eessRecord.get());
		return Optional.of(res);
	}
}
