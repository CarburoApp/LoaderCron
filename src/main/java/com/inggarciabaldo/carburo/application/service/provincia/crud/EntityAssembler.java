package com.inggarciabaldo.carburo.application.service.provincia.crud;

import com.inggarciabaldo.carburo.application.model.ComunidadAutonoma;
import com.inggarciabaldo.carburo.application.model.Provincia;
import com.inggarciabaldo.carburo.application.persistance.provincia.ProvinciaGateway.ProvinciaRecord;
import com.inggarciabaldo.carburo.application.service.util.crud.command.EntityAssemblerAuxiliar;

import java.util.List;

public class EntityAssembler {

	private EntityAssembler() {}

	public static List<Provincia> toEntityList(List<ProvinciaRecord> list) {
		return list.stream().map(EntityAssembler::toEntity).toList();
	}

	public static Provincia toEntity(ProvinciaRecord record) {
		ComunidadAutonoma comunidadAutonoma = EntityAssemblerAuxiliar.getComunidadAutonoma(record.idCCAA);
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
}

