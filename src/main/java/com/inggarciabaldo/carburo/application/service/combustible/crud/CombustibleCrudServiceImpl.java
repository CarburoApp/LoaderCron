package com.inggarciabaldo.carburo.application.service.combustible.crud;

import com.inggarciabaldo.carburo.application.model.Combustible;
import com.inggarciabaldo.carburo.application.service.combustible.CombustibleCrudService;
import com.inggarciabaldo.carburo.application.service.combustible.crud.commands.FindAllCombustibles;
import com.inggarciabaldo.carburo.application.service.util.crud.command.executor.JdbcCommandExecutor;

import java.util.List;


public class CombustibleCrudServiceImpl implements CombustibleCrudService {

	private final JdbcCommandExecutor executor = new JdbcCommandExecutor();

	@Override
	public List<Combustible> findAllCombustibles() {
		return executor.execute(new FindAllCombustibles());
	}
}
