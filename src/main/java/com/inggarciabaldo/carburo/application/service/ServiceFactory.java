package com.inggarciabaldo.carburo.application.service;

import com.inggarciabaldo.carburo.application.service.ccaa.ComunidadAutonomaCrudService;
import com.inggarciabaldo.carburo.application.service.ccaa.crud.ComunidadAutonomaCrudServiceImpl;
import com.inggarciabaldo.carburo.application.service.combustible.CombustibleCrudService;
import com.inggarciabaldo.carburo.application.service.combustible.crud.CombustibleCrudServiceImpl;
import com.inggarciabaldo.carburo.application.service.eess.EESSCrudService;
import com.inggarciabaldo.carburo.application.service.eess.crud.EESSCrudServiceImpl;
import com.inggarciabaldo.carburo.application.service.municipio.MunicipioCrudService;
import com.inggarciabaldo.carburo.application.service.municipio.crud.MunicipioCrudServiceImpl;
import com.inggarciabaldo.carburo.application.service.provincia.ProvinciaCrudService;
import com.inggarciabaldo.carburo.application.service.provincia.crud.ProvinciaCrudServiceImpl;

public class ServiceFactory {


	public ComunidadAutonomaCrudService forCCAAService() {
		return new ComunidadAutonomaCrudServiceImpl();
	}

	public ProvinciaCrudService forProvincia() {
		return new ProvinciaCrudServiceImpl();
	}

	public MunicipioCrudService forMunicipio() {
		return new MunicipioCrudServiceImpl();
	}

	public CombustibleCrudService forCombustible() {
		return new CombustibleCrudServiceImpl();
	}

	public EESSCrudService forEESS() {
		return new EESSCrudServiceImpl();
	}


}
