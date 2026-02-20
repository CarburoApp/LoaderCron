package app.carburo.loader.application.service.combustible.crud;

import app.carburo.loader.application.model.Combustible;
import app.carburo.loader.application.service.combustible.CombustibleCrudService;
import app.carburo.loader.application.service.combustible.crud.commands.FindAllCombustibles;
import app.carburo.loader.application.service.util.crud.command.executor.JdbcCommandExecutor;

import java.util.List;


public class CombustibleCrudServiceImpl implements CombustibleCrudService {

	private final JdbcCommandExecutor executor = new JdbcCommandExecutor();

	@Override
	public List<Combustible> findAllCombustibles() {
		return executor.execute(new FindAllCombustibles());
	}
}
