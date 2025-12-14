package com.inggarciabaldo.carburo.application.service.provincia.crud.commands;

import com.inggarciabaldo.carburo.application.Factorias;
import com.inggarciabaldo.carburo.application.model.Provincia;
import com.inggarciabaldo.carburo.application.persistance.provincia.ProvinciaGateway;
import com.inggarciabaldo.carburo.application.persistance.provincia.ProvinciaGateway.ProvinciaRecord;
import com.inggarciabaldo.carburo.application.service.provincia.crud.EntityAssembler;
import com.inggarciabaldo.carburo.application.service.util.command.Command;
import com.inggarciabaldo.carburo.config.cache.ApplicationCache;
import com.inggarciabaldo.carburo.util.log.Loggers;

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
