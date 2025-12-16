package com.inggarciabaldo.carburo.application.service.provincia.crud;

import com.inggarciabaldo.carburo.application.model.Provincia;
import com.inggarciabaldo.carburo.application.service.provincia.ProvinciaCrudService;
import com.inggarciabaldo.carburo.application.service.provincia.crud.commands.FindAllProvincias;
import com.inggarciabaldo.carburo.application.service.provincia.crud.commands.FindProvinciaByID;
import com.inggarciabaldo.carburo.application.service.util.crud.command.executor.JdbcCommandExecutor;

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
