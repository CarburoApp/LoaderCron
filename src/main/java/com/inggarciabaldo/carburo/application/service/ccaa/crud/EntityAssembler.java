package com.inggarciabaldo.carburo.application.service.ccaa.crud;

import com.inggarciabaldo.carburo.application.model.ComunidadAutonoma;
import com.inggarciabaldo.carburo.application.persistance.ccaa.CCAAGateway.CCAARecord;

import java.util.List;

public class EntityAssembler {

	public static List<ComunidadAutonoma> toEntityList(List<CCAARecord> list) {
		return list.stream().map(EntityAssembler::toEntity).toList();
	}

	public static ComunidadAutonoma toEntity(CCAARecord record) {
		return new ComunidadAutonoma(record.id, record.denominacion, record.extCode);
	}

	public static CCAARecord toRecord(ComunidadAutonoma entity) {
		CCAARecord m = new CCAARecord();
		m.id           = entity.getId();
		m.denominacion = entity.getDenominacion();
		m.extCode      = entity.getExtCode();
		return m;
	}
}

