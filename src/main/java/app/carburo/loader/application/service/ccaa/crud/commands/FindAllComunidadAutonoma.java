package app.carburo.loader.application.service.ccaa.crud.commands;

import app.carburo.loader.application.Factorias;
import app.carburo.loader.application.model.ComunidadAutonoma;
import app.carburo.loader.application.persistance.ccaa.CCAAGateway;
import app.carburo.loader.application.persistance.ccaa.CCAAGateway.CCAARecord;
import app.carburo.loader.application.service.ccaa.crud.EntityAssembler;
import app.carburo.loader.application.service.util.crud.command.Command;
import app.carburo.loader.config.cache.ApplicationCache;
import app.carburo.loader.util.log.Loggers;

import java.util.List;
import java.util.Map;

public class FindAllComunidadAutonoma implements Command<List<ComunidadAutonoma>> {

	@Override
	public List<ComunidadAutonoma> execute() {
		//Compruebo si está en caché
		Map<Short, ComunidadAutonoma> comunidadAutonomasCache = ApplicationCache.instance.getComunidadesAutonomas();
		if (comunidadAutonomasCache != null && !comunidadAutonomasCache.isEmpty())
			return List.copyOf(comunidadAutonomasCache.values());

		CCAAGateway mg = Factorias.persistence.forCCAA();
		List<CCAARecord> list = mg.findAll();
		List<ComunidadAutonoma> lista = EntityAssembler.toEntityList(list);
		Loggers.DB.info("CARGADAS todas las CCAA de la BD. Total: {}", list.size());
		// Lo añado a la caché
		ApplicationCache.instance.addCCAAs(lista);
		return lista;
	}
}
