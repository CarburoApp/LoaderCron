package com.inggarciabaldo.carburo.application.service.municipio.crud.commands;

import com.inggarciabaldo.carburo.application.Factorias;
import com.inggarciabaldo.carburo.application.model.Municipio;
import com.inggarciabaldo.carburo.application.persistance.municipio.MunicipioGateway;
import com.inggarciabaldo.carburo.application.persistance.municipio.MunicipioGateway.MunicipioRecord;
import com.inggarciabaldo.carburo.application.service.municipio.crud.EntityAssembler;
import com.inggarciabaldo.carburo.application.service.util.command.Command;
import com.inggarciabaldo.carburo.config.cache.ApplicationCache;
import com.inggarciabaldo.carburo.util.log.Loggers;

import java.util.List;
import java.util.Map;

public class FindAllMunicipios implements Command<List<Municipio>> {

	@Override
	public List<Municipio> execute() {
		// Comrpuebo si está en caché
		Map<Short, Municipio> cache = ApplicationCache.instance.getMunicipios();
		if (cache != null && !cache.isEmpty())
			return List.copyOf(cache.values());

		MunicipioGateway mg = Factorias.persistence.forMunicipio();
		List<MunicipioRecord> list = mg.findAll();
		List<Municipio> lista = EntityAssembler.toEntityList(list);
		Loggers.DB.info("Se han cargado todos los Municipios de la BD. Total: {}",
						list.size());
		// Lo añado a la caché
		ApplicationCache.instance.addMuncipios(lista);
		return lista;
	}
}
