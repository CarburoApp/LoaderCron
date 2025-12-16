package com.inggarciabaldo.carburo.application.service.combustible.crud.commands;

import com.inggarciabaldo.carburo.application.Factorias;
import com.inggarciabaldo.carburo.application.model.Combustible;
import com.inggarciabaldo.carburo.application.persistance.combustible.CombustibleGateway;
import com.inggarciabaldo.carburo.application.persistance.combustible.CombustibleGateway.CombustibleRecord;
import com.inggarciabaldo.carburo.application.service.combustible.crud.EntityAssembler;
import com.inggarciabaldo.carburo.application.service.util.crud.command.Command;
import com.inggarciabaldo.carburo.config.cache.ApplicationCache;
import com.inggarciabaldo.carburo.util.log.Loggers;

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
