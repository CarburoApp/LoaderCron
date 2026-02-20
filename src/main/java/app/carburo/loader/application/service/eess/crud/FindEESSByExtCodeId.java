package app.carburo.loader.application.service.eess.crud;

import app.carburo.loader.application.Factorias;
import app.carburo.loader.application.model.EstacionDeServicio;
import app.carburo.loader.application.persistance.eess.EESSGateway;
import app.carburo.loader.application.persistance.eess.EESSGateway.EESSRecord;
import app.carburo.loader.application.service.util.crud.command.Command;
import app.carburo.loader.util.log.Loggers;

import java.util.Optional;

public class FindEESSByExtCodeId implements Command<Optional<EstacionDeServicio>> {

	private final int extCodeId;


	public FindEESSByExtCodeId(int extCodeId) {
		if (extCodeId < 0)
			throw new IllegalArgumentException("El id de la EESS no puede ser negativo");
		this.extCodeId = extCodeId;
	}

	@Override
	public Optional<EstacionDeServicio> execute() {
		//Recuperamos la estación de servicio a traves del gateway
		EESSGateway mg = Factorias.persistence.forEESS();
		Optional<EESSRecord> eessRecord = mg.findByExtCode(this.extCodeId);
		Loggers.DB.info(
				"Se ha cargado la estación de servicio de la BD con extCodeId {}.",
				extCodeId);

		if (eessRecord.isEmpty()) return Optional.empty();

		EstacionDeServicio res = EntityAssembler.toEntity(eessRecord.get());
		return Optional.of(res);
	}
}