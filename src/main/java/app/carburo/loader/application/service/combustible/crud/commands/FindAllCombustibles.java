package app.carburo.loader.application.service.combustible.crud.commands;

import app.carburo.loader.application.Factorias;
import app.carburo.loader.application.model.Combustible;
import app.carburo.loader.application.persistance.combustible.CombustibleGateway;
import app.carburo.loader.application.persistance.combustible.CombustibleGateway.CombustibleRecord;
import app.carburo.loader.application.service.combustible.crud.EntityAssembler;
import app.carburo.loader.application.service.util.crud.command.Command;
import app.carburo.loader.config.cache.ApplicationCache;
import app.carburo.loader.util.log.Loggers;

import java.util.List;
import java.util.Set;

public class FindAllCombustibles implements Command<List<Combustible>> {

	@Override
	public List<Combustible> execute() {
		//Compruebo si está en caché
		Set<Combustible> combustiblesCache = ApplicationCache.instance.getTiposDeCombustible();
		if (combustiblesCache != null && !combustiblesCache.isEmpty())
			return List.copyOf(combustiblesCache);

		CombustibleGateway cg = Factorias.persistence.forCombustible();
		List<CombustibleRecord> list = cg.findAll();
		List<Combustible> lista = EntityAssembler.toEntityList(list);
		Loggers.DB.info(
				"CARGADOS todos los tipos-de-combustible de la BD. Total: {}",
				list.size());
		// Lo añado a la caché
		ApplicationCache.instance.addCombustibles(lista);
		return lista;
	}
}
