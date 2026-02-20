package app.carburo.loader.application.service.ccaa.crud;

import app.carburo.loader.application.model.ComunidadAutonoma;
import app.carburo.loader.application.service.ccaa.ComunidadAutonomaCrudService;
import app.carburo.loader.application.service.ccaa.crud.commands.FindAllComunidadAutonoma;
import app.carburo.loader.application.service.ccaa.crud.commands.FindComunidadAutonomaByID;
import app.carburo.loader.application.service.util.crud.command.executor.JdbcCommandExecutor;

import java.util.List;
import java.util.Optional;

public class ComunidadAutonomaCrudServiceImpl implements ComunidadAutonomaCrudService {

	private final JdbcCommandExecutor executor = new JdbcCommandExecutor();

	@Override
	public List<ComunidadAutonoma> findAllComunidadesAutonomas() {
		return executor.execute(new FindAllComunidadAutonoma());
	}

	@Override
	public Optional<ComunidadAutonoma> findComunidadAutonomaById(short id) {
		return executor.execute(new FindComunidadAutonomaByID(id));
	}
}
