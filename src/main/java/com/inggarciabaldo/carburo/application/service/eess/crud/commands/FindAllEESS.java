package com.inggarciabaldo.carburo.application.service.eess.crud.commands;

import com.inggarciabaldo.carburo.application.Factorias;
import com.inggarciabaldo.carburo.application.model.EstacionDeServicio;
import com.inggarciabaldo.carburo.application.persistance.eess.EESSGateway;
import com.inggarciabaldo.carburo.application.persistance.eess.EESSGateway.EESSRecord;
import com.inggarciabaldo.carburo.application.service.eess.crud.EntityAssembler;
import com.inggarciabaldo.carburo.application.service.util.command.Command;
import com.inggarciabaldo.carburo.util.log.Loggers;

import java.util.List;

public class FindAllEESS implements Command<List<EstacionDeServicio>> {

	@Override
	public List<EstacionDeServicio> execute() {
		//Recuperamos las estaciones de servicio a traves del gateway
		EESSGateway mg = Factorias.persistence.forEESS();
		List<EESSRecord> list = mg.findAll();
		Loggers.DB.info("CARGADOS todas los EESS de la BD. Total: {}", list.size());
		return EntityAssembler.toEntityList(list);
	}

}
