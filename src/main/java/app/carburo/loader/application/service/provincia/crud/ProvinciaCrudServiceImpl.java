package app.carburo.loader.application.service.provincia.crud;

import app.carburo.loader.application.model.Provincia;
import app.carburo.loader.application.service.provincia.ProvinciaCrudService;
import app.carburo.loader.application.service.provincia.crud.commands.FindAllProvincias;
import app.carburo.loader.application.service.provincia.crud.commands.FindProvinciaByID;
import app.carburo.loader.application.service.util.crud.command.executor.JdbcCommandExecutor;

import java.util.List;
import java.util.Optional;

public class ProvinciaCrudServiceImpl implements ProvinciaCrudService {

	private final JdbcCommandExecutor executor = new JdbcCommandExecutor();

	@Override
	public List<Provincia> findAllProvincias() {
		return executor.execute(new FindAllProvincias());
	}

	@Override
	public Optional<Provincia> findProvinciaById(short id) {
		return executor.execute(new FindProvinciaByID(id));
	}
}
