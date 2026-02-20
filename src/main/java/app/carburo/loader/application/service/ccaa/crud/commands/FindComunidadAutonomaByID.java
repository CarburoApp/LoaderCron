package app.carburo.loader.application.service.ccaa.crud.commands;

import app.carburo.loader.application.Factorias;
import app.carburo.loader.application.model.ComunidadAutonoma;
import app.carburo.loader.application.persistance.ccaa.CCAAGateway;
import app.carburo.loader.application.persistance.ccaa.CCAAGateway.CCAARecord;
import app.carburo.loader.application.service.ccaa.crud.EntityAssembler;
import app.carburo.loader.application.service.util.crud.command.Command;
import app.carburo.loader.config.cache.ApplicationCache;
import app.carburo.loader.util.log.Loggers;

import java.util.Optional;

public class FindComunidadAutonomaByID implements Command<Optional<ComunidadAutonoma>> {

	private final short id;

	public FindComunidadAutonomaByID(short id) {
		if (id < 0) throw new IllegalArgumentException(
				"El id del ccaa no puede ser negativo. Id: " + id);
		this.id = id;
	}

	@Override
	public Optional<ComunidadAutonoma> execute() {
		//Compruebo si está en caché
		ComunidadAutonoma comunidadAutonomaCache = ApplicationCache.instance.getComunidadAutonomaById(
				id);
		if (comunidadAutonomaCache != null) return Optional.of(comunidadAutonomaCache);

		//Recuperamos el ccaa a traves del gateway
		CCAAGateway mg = Factorias.persistence.forCCAA();
		Optional<CCAARecord> ccaaRecord = mg.findById(String.valueOf(this.id));
		Loggers.DB.info("Se ha cargado el Municipio de la BD con id {}.", id);

		if (ccaaRecord.isEmpty()) return Optional.empty();
		ComunidadAutonoma res = EntityAssembler.toEntity(ccaaRecord.get());
		//No lo añado a la caché debido a que no podría diferenciar entre si ya tengo la lista completa en caché o no
		return Optional.of(res);
	}
}
