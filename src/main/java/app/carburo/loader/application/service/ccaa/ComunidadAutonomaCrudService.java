package app.carburo.loader.application.service.ccaa;

import app.carburo.loader.application.model.ComunidadAutonoma;

import java.util.List;
import java.util.Optional;

public interface ComunidadAutonomaCrudService {

	List<ComunidadAutonoma> findAllComunidadesAutonomas();

	Optional<ComunidadAutonoma> findComunidadAutonomaById(short id);
}
