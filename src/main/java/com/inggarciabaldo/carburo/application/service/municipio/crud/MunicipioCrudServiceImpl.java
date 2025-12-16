package com.inggarciabaldo.carburo.application.service.municipio.crud;

import com.inggarciabaldo.carburo.application.model.Municipio;
import com.inggarciabaldo.carburo.application.service.municipio.MunicipioCrudService;
import com.inggarciabaldo.carburo.application.service.municipio.crud.commands.FindAllMunicipios;
import com.inggarciabaldo.carburo.application.service.municipio.crud.commands.FindMunicipioById;
import com.inggarciabaldo.carburo.application.service.util.crud.command.executor.JdbcCommandExecutor;

import java.util.List;
import java.util.Optional;

public class MunicipioCrudServiceImpl implements MunicipioCrudService {

	private final JdbcCommandExecutor executor = new JdbcCommandExecutor();

	@Override
	public List<Municipio> findAllMunicipios() {
		return executor.execute(new FindAllMunicipios());
	}

	@Override
	public Optional<Municipio> findMunicipioById(short id) {
		return executor.execute(new FindMunicipioById(id));
	}
}
