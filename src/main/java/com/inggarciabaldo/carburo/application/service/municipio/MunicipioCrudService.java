package com.inggarciabaldo.carburo.application.service.municipio;

import com.inggarciabaldo.carburo.application.model.Municipio;

import java.util.List;
import java.util.Optional;

public interface MunicipioCrudService {

	List<Municipio> findAllMunicipios();

	Optional<Municipio> findMunicipioById(short id);
}
