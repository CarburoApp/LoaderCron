package com.inggarciabaldo.carburo.application.service.eess.crud;

import com.inggarciabaldo.carburo.application.model.EstacionDeServicio;
import com.inggarciabaldo.carburo.application.service.eess.EESSCrudService;
import com.inggarciabaldo.carburo.application.service.eess.crud.commands.AddEESS;
import com.inggarciabaldo.carburo.application.service.eess.crud.commands.FindAllEESS;
import com.inggarciabaldo.carburo.application.service.eess.crud.commands.FindEESSById;
import com.inggarciabaldo.carburo.application.service.util.command.executor.JdbcCommandExecutor;

import java.util.List;
import java.util.Optional;

public class EESSCrudServiceImpl implements EESSCrudService {

	private final JdbcCommandExecutor executor = new JdbcCommandExecutor();


	@Override
	public EstacionDeServicio addEESS(EstacionDeServicio estacionDeServicio) {
		return executor.execute(new AddEESS(estacionDeServicio));
	}

	@Override
	public Optional<EstacionDeServicio> findEESSById(String id) {
		return executor.execute(new FindEESSById(id));
	}

	@Override
	public Optional<EstacionDeServicio> findEESSByExtCode(int extCodeId) {
		return executor.execute(new FindEESSByExtCodeId(extCodeId));
	}


	@Override
	public List<EstacionDeServicio> findAllEESS() {
		return executor.execute(new FindAllEESS());
	}
}
