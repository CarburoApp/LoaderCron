package com.inggarciabaldo.carburo.application.service.eess.crud;


import com.inggarciabaldo.carburo.application.Factorias;
import com.inggarciabaldo.carburo.application.model.EstacionDeServicio;
import com.inggarciabaldo.carburo.application.model.Municipio;
import com.inggarciabaldo.carburo.application.model.PrecioCombustible;
import com.inggarciabaldo.carburo.application.model.Provincia;
import com.inggarciabaldo.carburo.application.model.enums.Margen;
import com.inggarciabaldo.carburo.application.model.enums.Remision;
import com.inggarciabaldo.carburo.application.model.enums.Venta;
import com.inggarciabaldo.carburo.application.persistance.eess.EESSGateway.EESSRecord;
import com.inggarciabaldo.carburo.application.persistance.preciocombustible.PrecioCombustibleGateway.PrecioCombustibleRecord;
import com.inggarciabaldo.carburo.application.service.util.crud.command.EntityAssemblerAuxiliar;
import com.inggarciabaldo.carburo.util.log.Loggers;

import java.sql.Date;
import java.util.List;

public class EntityAssembler {

	private EntityAssembler() {}

	public static List<EstacionDeServicio> toEntityList(List<EESSRecord> list) {
		return list.stream().map(EntityAssembler::toEntity).toList();
	}

	public static EstacionDeServicio toEntity(EESSRecord record) {

		Municipio municipio = EntityAssemblerAuxiliar.getMunicipio(record.idMunicipio);
		Provincia provincia = EntityAssemblerAuxiliar.getProvincia(record.idProvincia);

		// Compruebo si coincide la provincia del municipio con la provincia del EESS
		if (!provincia.equals(municipio.getProvincia())) {
			Loggers.DB.warn(
					"La provincia del municipio no coincide con la provincia de la EESS. " +
							"Id(EESS): {}, Id(Municipio): {}, Id(Provincia EESS): {}, " +
							"Id(Provincia Municipio): {}", record.id, municipio.getId(),
					provincia.getId(), municipio.getProvincia().getId());
		}

		double latitud = record.latitud;
		double longitud = record.longitud;

		Margen margen = Factorias.enumFromCode.getMargenFromCodeTool()
				.fromCode(record.margen);
		Remision remision = Factorias.enumFromCode.getRemisionFromCodeTool()
				.fromCode(record.remision);
		Venta venta = Factorias.enumFromCode.getVentaFromCodeTool()
				.fromCode(record.venta);
		double x100BioEtanol = record.x100BioEtanol;
		double x100EsterMetilico = record.x100EsterMetilico;

		// No se tiene en cuenta el combustible disponible
		return new EstacionDeServicio(record.id, record.extCode, record.rotulo, record.horario,
									  record.direccion, record.localidad, record.codigoPostal, municipio,
									  provincia, latitud, longitud, margen, remision, venta,
									  x100BioEtanol, x100EsterMetilico);
	}

	public static EESSRecord toRecord(EstacionDeServicio entity) {
		EESSRecord m = new EESSRecord();

		m.id                = entity.getId();
		m.extCode           = entity.getExtCode();
		m.rotulo            = entity.getRotulo();
		m.horario           = entity.getHorario();
		m.direccion         = entity.getDireccion();
		m.localidad         = entity.getLocalidad();
		m.codigoPostal      = entity.getCodigoPostal();
		m.idMunicipio       = entity.getMunicipio().getId();
		m.idProvincia       = entity.getProvincia().getId();
		m.latitud           = entity.getLatitud();
		m.longitud          = entity.getLongitud();
		m.remision          = entity.getRemision().getCode();
		m.margen            = entity.getMargen().getCode();
		m.venta             = entity.getVenta().getCode();
		m.x100BioEtanol     = entity.getX100BioEtanol();
		m.x100EsterMetilico = entity.getX100EsterMetilico();

		return m;
	}

	public static PrecioCombustibleRecord toPrecioCombustibleRecord(
			PrecioCombustible entity) {
		PrecioCombustibleRecord m = new PrecioCombustibleRecord();
		m.idCombustible = entity.getCombustible().getId();
		m.idEESS        = entity.getEstacionDeServicio().getId();
		m.fecha         = Date.valueOf(entity.getFecha());
		m.precio        = entity.getPrecio();
		return m;
	}
}

