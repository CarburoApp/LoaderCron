package com.inggarciabaldo.carburo.application.service.provincia.crud.commands;

import com.inggarciabaldo.carburo.application.Factorias;
import com.inggarciabaldo.carburo.application.model.Provincia;
import com.inggarciabaldo.carburo.application.persistance.provincia.ProvinciaGateway;
import com.inggarciabaldo.carburo.application.persistance.provincia.ProvinciaGateway.ProvinciaRecord;
import com.inggarciabaldo.carburo.application.service.provincia.crud.EntityAssembler;
import com.inggarciabaldo.carburo.application.service.util.command.Command;
import com.inggarciabaldo.carburo.config.cache.ApplicationCache;
import com.inggarciabaldo.carburo.util.log.Loggers;

import java.util.List;
import java.util.Map;

public class FindAllProvincias implements Command<List<Provincia>> {

	@Override
	public List<Provincia> execute() {
		// Comrpuebo si está en caché
		Map<Short, Provincia> cache = ApplicationCache.instance.getProvincias();
		if (cache != null && !cache.isEmpty())
			return List.copyOf(cache.values());

		//Recuperamos la provincia a traves del gateway
		ProvinciaGateway mg = Factorias.persistence.forProvincia();
		List<ProvinciaRecord> list = mg.findAll();
		List<Provincia> lista = EntityAssembler.toEntityList(list);
		Loggers.DB.info("Se han cargado todas las Provincias de la BD. Total: {}",
						list.size());
		// Lo añado a la caché
		ApplicationCache.instance.addProvincias(lista);
		return lista;
	}
}
