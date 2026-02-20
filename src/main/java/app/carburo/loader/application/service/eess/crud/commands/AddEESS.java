package app.carburo.loader.application.service.eess.crud.commands;

import app.carburo.loader.application.Factorias;
import app.carburo.loader.application.model.Combustible;
import app.carburo.loader.application.model.EstacionDeServicio;
import app.carburo.loader.application.model.PrecioCombustible;
import app.carburo.loader.application.persistance.eess.EESSGateway;
import app.carburo.loader.application.persistance.eess.EESSGateway.EESSRecord;
import app.carburo.loader.application.service.eess.crud.EntityAssembler;
import app.carburo.loader.application.service.util.crud.command.Command;
import app.carburo.loader.util.log.Loggers;

import java.util.Optional;

public class AddEESS implements Command<EstacionDeServicio> {

	private final EstacionDeServicio eess;

	public AddEESS(EstacionDeServicio estacionDeServicio) {
		if (estacionDeServicio == null)
			throw new IllegalArgumentException("La EESS no puede ser nulo");
		this.eess = estacionDeServicio;
	}

	@Override
	public EstacionDeServicio execute() {
		//Añadimos la eess a traves del gateway
		EESSGateway mg = Factorias.persistence.forEESS();

		// Comprobamos si ya existe una EESS con el mismo código externo
		Optional<EESSRecord> eessBD = mg.findByExtCode(eess.getExtCode());

		// Solo si no se encuentra la estación en BD, persistimos
		if (eessBD.isPresent()) throw new IllegalStateException(
				"La estación de servicio con código externo " + eess.getExtCode() +
						" ya existe en la base de datos con id " + eessBD.get().id + ".");

		Long eessID = mg.add(EntityAssembler.toRecord(eess));
		Loggers.DB.info(
				"Se ha insertado la estación de servicio de la BD con id externa {} - id generada: {}.",
				eess.getExtCode(), eessID);

		if (eessID == null) throw new IllegalStateException(
				"No se ha podido insertar la estación de servicio en la base de datos.");

		this.eess.setId(eessID.intValue());

		//Comprobamos si tiene Disponibilidad de combustibles asociada
		if (!eess.getCombustiblesDisponibles().isEmpty()) {
			for (Combustible combustible : eess.getCombustiblesDisponibles()) {
				Factorias.persistence.forCombustibleDisponible()
						.add(combustible.getId(), eessID.intValue());
			}
		}

		//Comprobamos si tiene Precios de combustibles asociada
		if (!eess.getPreciosCombustibles().isEmpty()) {
			for (PrecioCombustible precioCombustible : eess.getPreciosCombustibles()) {
				Factorias.persistence.forPrecioCombustible()
						.add(EntityAssembler.toPrecioCombustibleRecord(precioCombustible));
			}
		}

		return eess;
	}
}
