package app.carburo.loader.application.service.municipio.crud.commands;

import app.carburo.loader.application.Factorias;
import app.carburo.loader.application.model.Municipio;
import app.carburo.loader.application.persistance.municipio.MunicipioGateway;
import app.carburo.loader.application.persistance.municipio.MunicipioGateway.MunicipioRecord;
import app.carburo.loader.application.service.municipio.crud.EntityAssembler;
import app.carburo.loader.application.service.util.crud.command.Command;
import app.carburo.loader.config.cache.ApplicationCache;
import app.carburo.loader.util.log.Loggers;

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
		Loggers.DB.info("CARGADOS todos los Municipios de la BD. Total: {}",
						list.size());
		// Lo añado a la caché
		ApplicationCache.instance.addMuncipios(lista);
		return lista;
	}
}
