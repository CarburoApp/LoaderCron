package com.inggarciabaldo.carburo.application.service.eess;

import com.inggarciabaldo.carburo.application.model.EstacionDeServicio;

import java.util.List;
import java.util.Optional;

public interface EESSCrudService {

	EstacionDeServicio addEESS(EstacionDeServicio estacionDeServicio);

	Optional<EstacionDeServicio> findEESSById(String id);

	List<EstacionDeServicio> findAllEESS();
}
