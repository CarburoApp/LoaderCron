package com.inggarciabaldo.carburo.application.service.combustible.crud;

import com.inggarciabaldo.carburo.application.model.Combustible;
import com.inggarciabaldo.carburo.application.persistance.combustible.CombustibleGateway.CombustibleRecord;

import java.util.List;

public class EntityAssembler {

	public static List<Combustible> toEntityList(List<CombustibleRecord> list) {
		return list.stream().map(EntityAssembler::toEntity).toList();
	}

	public static Combustible toEntity(CombustibleRecord record) {
		return new Combustible(record.id, record.denominacion, record.codigo,
							   record.extCode);
	}

	public static CombustibleRecord toRecord(Combustible entity) {
		CombustibleRecord m = new CombustibleRecord();
		m.id           = entity.getId();
		m.denominacion = entity.getDenominacion();
		m.extCode      = entity.getExtCode();
		m.codigo       = entity.getCodigo();
		return m;
	}
}

