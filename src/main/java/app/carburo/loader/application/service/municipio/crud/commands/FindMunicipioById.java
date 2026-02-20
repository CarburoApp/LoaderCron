package app.carburo.loader.application.service.municipio.crud.commands;

import app.carburo.loader.application.Factorias;
import app.carburo.loader.application.model.Municipio;
import app.carburo.loader.application.persistance.municipio.MunicipioGateway;
import app.carburo.loader.application.persistance.municipio.MunicipioGateway.MunicipioRecord;
import app.carburo.loader.application.service.municipio.crud.EntityAssembler;
import app.carburo.loader.application.service.util.crud.command.Command;
import app.carburo.loader.config.cache.ApplicationCache;
import app.carburo.loader.util.log.Loggers;

import java.util.Optional;

public class FindMunicipioById implements Command<Optional<Municipio>> {

	private final short id;

	public FindMunicipioById(short id) {
		if (id >= 0) this.id = id;
		throw new IllegalArgumentException("El id del municipio no puede ser negativo");
	}

	@Override
	public Optional<Municipio> execute() {
		//Compruebo si está en caché
		Municipio municipioCache = ApplicationCache.instance.getMunicipioById(id);
		if (municipioCache != null) return Optional.of(municipioCache);

		//Recuperamos el municipio a traves del gateway
		MunicipioGateway mg = Factorias.persistence.forMunicipio();
		Optional<MunicipioRecord> municipioRecord = mg.findById(String.valueOf(this.id));
		Loggers.DB.info("Se ha cargado el Municipio de la BD con id {}.", id);

		if (municipioRecord.isEmpty()) return Optional.empty();
		Municipio res = EntityAssembler.toEntity(municipioRecord.get());
		//No lo añado a la caché debido a que no podría diferenciar entre si ya tengo la lista completa en caché o no
		return Optional.of(res);
	}
}
