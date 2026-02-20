package app.carburo.loader.application.service.provincia.crud.commands;

import app.carburo.loader.application.Factorias;
import app.carburo.loader.application.model.Provincia;
import app.carburo.loader.application.persistance.provincia.ProvinciaGateway;
import app.carburo.loader.application.persistance.provincia.ProvinciaGateway.ProvinciaRecord;
import app.carburo.loader.application.service.provincia.crud.EntityAssembler;
import app.carburo.loader.application.service.util.crud.command.Command;
import app.carburo.loader.config.cache.ApplicationCache;
import app.carburo.loader.util.log.Loggers;

import java.util.Optional;

public class FindProvinciaByID implements Command<Optional<Provincia>> {

	private final short id;

	public FindProvinciaByID(short id) {
		if (id < 0) throw new IllegalArgumentException(
				"El id de la provincia no puede ser negativo");
		this.id = id;
	}

	@Override
	public Optional<Provincia> execute() {
		//Compruebo si está en caché
		Provincia provinciaCache = ApplicationCache.instance.getProvinciaById(id);
		if (provinciaCache != null) return Optional.of(provinciaCache);

		//Recuperamos la provincia a traves del gateway
		ProvinciaGateway mg = Factorias.persistence.forProvincia();
		Optional<ProvinciaRecord> provinciaRecord = mg.findById(String.valueOf(this.id));
		Loggers.DB.info("Se ha cargado la Provincia de la BD con id {}.", id);

		if (provinciaRecord.isEmpty()) return Optional.empty();
		Provincia res = EntityAssembler.toEntity(provinciaRecord.get());
		//No lo añado a la caché debido a que no podría diferenciar entre si ya tengo la lista completa en caché o no
		return Optional.of(res);
	}
}
