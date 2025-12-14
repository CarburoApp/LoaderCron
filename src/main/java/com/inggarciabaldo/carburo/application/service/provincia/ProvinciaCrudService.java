package com.inggarciabaldo.carburo.application.service.provincia;

import com.inggarciabaldo.carburo.application.model.Provincia;

import java.util.List;
import java.util.Optional;

public interface ProvinciaCrudService {

	List<Provincia> findAllProvincias();

	Optional<Provincia> findProvinciaById(short id);
}
