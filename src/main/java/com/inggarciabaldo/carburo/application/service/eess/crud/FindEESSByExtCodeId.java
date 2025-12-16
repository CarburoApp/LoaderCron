package com.inggarciabaldo.carburo.application.service.eess.crud;

import com.inggarciabaldo.carburo.application.Factorias;
import com.inggarciabaldo.carburo.application.model.EstacionDeServicio;
import com.inggarciabaldo.carburo.application.persistance.eess.EESSGateway;
import com.inggarciabaldo.carburo.application.persistance.eess.EESSGateway.EESSRecord;
import com.inggarciabaldo.carburo.application.service.util.crud.command.Command;
import com.inggarciabaldo.carburo.util.log.Loggers;

import java.util.Optional;

public class FindEESSByExtCodeId implements Command<Optional<EstacionDeServicio>> {

	private final int extCodeId;


	public FindEESSByExtCodeId(int extCodeId) {
		if (extCodeId < 0)
			throw new IllegalArgumentException("El id de la EESS no puede ser negativo");
		this.extCodeId = extCodeId;
	}

	@Override
	public Optional<EstacionDeServicio> execute() {
		//Recuperamos la estación de servicio a traves del gateway
		EESSGateway mg = Factorias.persistence.forEESS();
		Optional<EESSRecord> eessRecord = mg.findByExtCode(this.extCodeId);
		Loggers.DB.info(
				"Se ha cargado la estación de servicio de la BD con extCodeId {}.",
				extCodeId);

		if (eessRecord.isEmpty()) return Optional.empty();

		EstacionDeServicio res = EntityAssembler.toEntity(eessRecord.get());
		return Optional.of(res);
	}
}