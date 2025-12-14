package com.inggarciabaldo.carburo.application.service.ccaa;

import com.inggarciabaldo.carburo.application.model.ComunidadAutonoma;

import java.util.List;
import java.util.Optional;

public interface ComunidadAutonomaCrudService {

	List<ComunidadAutonoma> findAllComunidadesAutonomas();

	Optional<ComunidadAutonoma> findComunidadAutonomaById(short id);
}
