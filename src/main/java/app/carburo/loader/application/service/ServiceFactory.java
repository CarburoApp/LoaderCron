package app.carburo.loader.application.service;

import app.carburo.loader.application.service.ccaa.ComunidadAutonomaCrudService;
import app.carburo.loader.application.service.ccaa.crud.ComunidadAutonomaCrudServiceImpl;
import app.carburo.loader.application.service.combustible.CombustibleCrudService;
import app.carburo.loader.application.service.combustible.crud.CombustibleCrudServiceImpl;
import app.carburo.loader.application.service.eess.EESSCrudService;
import app.carburo.loader.application.service.eess.crud.EESSCrudServiceImpl;
import app.carburo.loader.application.service.municipio.MunicipioCrudService;
import app.carburo.loader.application.service.municipio.crud.MunicipioCrudServiceImpl;
import app.carburo.loader.application.service.provincia.ProvinciaCrudService;
import app.carburo.loader.application.service.provincia.crud.ProvinciaCrudServiceImpl;

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
