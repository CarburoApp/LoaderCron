package app.carburo.loader.application.service.eess.crud.commands;

import app.carburo.loader.application.Factorias;
import app.carburo.loader.application.model.EstacionDeServicio;
import app.carburo.loader.application.persistance.eess.EESSGateway;
import app.carburo.loader.application.persistance.eess.EESSGateway.EESSRecord;
import app.carburo.loader.application.service.eess.crud.EntityAssembler;
import app.carburo.loader.application.service.util.crud.command.Command;
import app.carburo.loader.util.log.Loggers;

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
