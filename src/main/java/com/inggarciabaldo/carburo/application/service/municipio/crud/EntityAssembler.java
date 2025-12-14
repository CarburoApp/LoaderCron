package com.inggarciabaldo.carburo.application.service.municipio.crud;


import com.inggarciabaldo.carburo.application.Factorias;
import com.inggarciabaldo.carburo.application.model.Municipio;
import com.inggarciabaldo.carburo.application.model.Provincia;
import com.inggarciabaldo.carburo.application.persistance.municipio.MunicipioGateway.MunicipioRecord;
import com.inggarciabaldo.carburo.config.cache.ApplicationCache;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EntityAssembler {

	public static List<Municipio> toEntityList(List<MunicipioRecord> list) {
		return list.stream().map(EntityAssembler::toEntity).toList();
	}

	public static Municipio toEntity(MunicipioRecord record) {
		Provincia provincia = getProvincia(record.idProvincia);
		return new Municipio(record.id, record.denominacion, record.extCode, provincia);
	}

	public static MunicipioRecord toRecord(Municipio entity) {
		MunicipioRecord m = new MunicipioRecord();
		m.id           = entity.getId();
		m.denominacion = entity.getDenominacion();
		m.extCode      = entity.getExtCode();
		m.idProvincia  = entity.getProvincia().getId();
		return m;
	}

	private static Provincia getProvincia(short id) {
		// Comrpuebo si está en caché
		Map<Short, Provincia> cache = ApplicationCache.instance.getProvincias();
		if (cache != null && !cache.isEmpty()) for (Provincia provincia : cache.values())
			if (provincia.getExtCode() == id) return provincia;

		Optional<Provincia> provincia = Factorias.service.forProvincia()
				.findProvinciaById(id);
		if (provincia.isEmpty()) throw new IllegalStateException(
				"No se ha encontrado la provincia del municipio. Id(provincia): " + id);
		return provincia.get();
	}
}