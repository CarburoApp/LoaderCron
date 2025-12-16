package com.inggarciabaldo.carburo.application.service.ccaa.crud;

import com.inggarciabaldo.carburo.application.model.ComunidadAutonoma;
import com.inggarciabaldo.carburo.application.service.ccaa.ComunidadAutonomaCrudService;
import com.inggarciabaldo.carburo.application.service.ccaa.crud.commands.FindAllComunidadAutonoma;
import com.inggarciabaldo.carburo.application.service.ccaa.crud.commands.FindComunidadAutonomaByID;
import com.inggarciabaldo.carburo.application.service.util.crud.command.executor.JdbcCommandExecutor;

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
