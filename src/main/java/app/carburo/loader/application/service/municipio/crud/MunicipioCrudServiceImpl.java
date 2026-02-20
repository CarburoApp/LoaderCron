package app.carburo.loader.application.service.municipio.crud;

import app.carburo.loader.application.model.Municipio;
import app.carburo.loader.application.service.municipio.MunicipioCrudService;
import app.carburo.loader.application.service.municipio.crud.commands.FindAllMunicipios;
import app.carburo.loader.application.service.municipio.crud.commands.FindMunicipioById;
import app.carburo.loader.application.service.util.crud.command.executor.JdbcCommandExecutor;

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
