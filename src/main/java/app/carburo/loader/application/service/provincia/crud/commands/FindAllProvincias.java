package app.carburo.loader.application.service.provincia.crud.commands;

import app.carburo.loader.application.Factorias;
import app.carburo.loader.application.model.Provincia;
import app.carburo.loader.application.persistance.provincia.ProvinciaGateway;
import app.carburo.loader.application.persistance.provincia.ProvinciaGateway.ProvinciaRecord;
import app.carburo.loader.application.service.provincia.crud.EntityAssembler;
import app.carburo.loader.application.service.util.crud.command.Command;
import app.carburo.loader.config.cache.ApplicationCache;
import app.carburo.loader.util.log.Loggers;

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
		Loggers.DB.info("CARGADAS todas las Provincias de la BD. Total: {}",
						list.size());
		// Lo añado a la caché
		ApplicationCache.instance.addProvincias(lista);
		return lista;
	}
}
