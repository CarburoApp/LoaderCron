package app.carburo.loader.application.service.provincia;

import app.carburo.loader.application.model.Provincia;

import java.util.List;
import java.util.Optional;

public interface ProvinciaCrudService {

	List<Provincia> findAllProvincias();

	Optional<Provincia> findProvinciaById(short id);
}
