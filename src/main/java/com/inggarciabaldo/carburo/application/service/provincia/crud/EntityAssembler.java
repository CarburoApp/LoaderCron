package com.inggarciabaldo.carburo.application.service.provincia.crud;

import com.inggarciabaldo.carburo.application.Factorias;
import com.inggarciabaldo.carburo.application.model.ComunidadAutonoma;
import com.inggarciabaldo.carburo.application.model.Provincia;
import com.inggarciabaldo.carburo.application.persistance.provincia.ProvinciaGateway.ProvinciaRecord;
import com.inggarciabaldo.carburo.config.cache.ApplicationCache;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EntityAssembler {

	private EntityAssembler() {}

	public static List<Provincia> toEntityList(List<ProvinciaRecord> list) {
		return list.stream().map(EntityAssembler::toEntity).toList();
	}

	public static Provincia toEntity(ProvinciaRecord record) {
		ComunidadAutonoma comunidadAutonoma = getComunidadAutonoma(record.idCCAA);
		return new Provincia(record.id, record.denominacion, record.extCode,
							 comunidadAutonoma);
	}

	public static ProvinciaRecord toRecord(Provincia entity) {
		ProvinciaRecord m = new ProvinciaRecord();
		m.id           = entity.getId();
		m.denominacion = entity.getDenominacion();
		m.extCode      = entity.getExtCode();
		m.idCCAA       = entity.getComunidadAutonoma().getId();
		return m;
	}

	private static ComunidadAutonoma getComunidadAutonoma(short id) {
		// Comrpuebo si está en caché
		Map<Short, ComunidadAutonoma> cache = ApplicationCache.instance.getComunidadesAutonomas();
		if (cache != null && !cache.isEmpty())
			for (ComunidadAutonoma ccaa : cache.values())
				if (ccaa.getExtCode() == id) return ccaa;

		Optional<ComunidadAutonoma> ca = Factorias.service.forCCAAService()
				.findComunidadAutonomaById(id);
		if (ca.isEmpty()) throw new IllegalStateException(
				"No se ha encontrado la comunidad autonoma de la provincia. Id(ccaa): " +
						id);
		return ca.get();
	}
}

