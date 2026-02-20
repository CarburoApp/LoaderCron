package app.carburo.loader.application.service.municipio.crud;


import app.carburo.loader.application.model.Municipio;
import app.carburo.loader.application.model.Provincia;
import app.carburo.loader.application.persistance.municipio.MunicipioGateway.MunicipioRecord;
import app.carburo.loader.application.service.util.crud.command.EntityAssemblerAuxiliar;

import java.util.List;

public class EntityAssembler {

	private EntityAssembler() {}

	public static List<Municipio> toEntityList(List<MunicipioRecord> list) {
		return list.stream().map(EntityAssembler::toEntity).toList();
	}

	public static Municipio toEntity(MunicipioRecord record) {
		Provincia provincia = EntityAssemblerAuxiliar.getProvincia(record.idProvincia);
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
}