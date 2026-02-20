package app.carburo.loader.application.service.municipio;

import app.carburo.loader.application.model.Municipio;

import java.util.List;
import java.util.Optional;

public interface MunicipioCrudService {

	List<Municipio> findAllMunicipios();

	Optional<Municipio> findMunicipioById(short id);
}
