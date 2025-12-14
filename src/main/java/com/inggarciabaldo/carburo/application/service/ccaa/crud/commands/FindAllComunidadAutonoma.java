package com.inggarciabaldo.carburo.application.service.ccaa.crud.commands;

import com.inggarciabaldo.carburo.application.Factorias;
import com.inggarciabaldo.carburo.application.model.ComunidadAutonoma;
import com.inggarciabaldo.carburo.application.persistance.ccaa.CCAAGateway;
import com.inggarciabaldo.carburo.application.persistance.ccaa.CCAAGateway.CCAARecord;
import com.inggarciabaldo.carburo.application.service.ccaa.crud.EntityAssembler;
import com.inggarciabaldo.carburo.application.service.util.command.Command;
import com.inggarciabaldo.carburo.config.cache.ApplicationCache;
import com.inggarciabaldo.carburo.util.log.Loggers;

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
		Loggers.DB.info("Se han cargado todas las CCAA de la BD. Total: {}", list.size());
		// Lo añado a la caché
		ApplicationCache.instance.addCCAAs(lista);
		return lista;
	}
}
