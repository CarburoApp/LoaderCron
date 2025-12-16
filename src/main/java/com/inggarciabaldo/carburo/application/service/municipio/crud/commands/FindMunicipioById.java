package com.inggarciabaldo.carburo.application.service.municipio.crud.commands;

import com.inggarciabaldo.carburo.application.Factorias;
import com.inggarciabaldo.carburo.application.model.Municipio;
import com.inggarciabaldo.carburo.application.persistance.municipio.MunicipioGateway;
import com.inggarciabaldo.carburo.application.persistance.municipio.MunicipioGateway.MunicipioRecord;
import com.inggarciabaldo.carburo.application.service.municipio.crud.EntityAssembler;
import com.inggarciabaldo.carburo.application.service.util.crud.command.Command;
import com.inggarciabaldo.carburo.config.cache.ApplicationCache;
import com.inggarciabaldo.carburo.util.log.Loggers;

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
