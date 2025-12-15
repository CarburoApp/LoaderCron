package com.inggarciabaldo.carburo.application.service.eess.crud;

import com.inggarciabaldo.carburo.application.model.EstacionDeServicio;
import com.inggarciabaldo.carburo.application.service.eess.EESSCrudService;
import com.inggarciabaldo.carburo.application.service.eess.crud.commands.*;
import com.inggarciabaldo.carburo.application.service.util.command.executor.JdbcCommandExecutor;
import com.inggarciabaldo.carburo.application.service.util.dto.CombustibleDisponibleDTO;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class EESSCrudServiceImpl implements EESSCrudService {

	private final JdbcCommandExecutor executor = new JdbcCommandExecutor();


	@Override
	public EstacionDeServicio addEESS(EstacionDeServicio estacionDeServicio) {
		return executor.execute(new AddEESS(estacionDeServicio));
	}

	/**
	 * Encargado de realizar la inserción masiva de estaciones de servicio, sus disponibilidades y precios de combustibles.
	 * Todos los datos se dan por comprobados, supiniendo que se ha verificado que no hay problemas de integridad referencial ni de duplicados.
	 *
	 * @return Número de estaciones de servicio insertadas satisfactoriamente.
	 */
	@Override
	public int addAllEESSandDisponCombusAndPrecioCombus(
			Collection<EstacionDeServicio> estacionDeServicios) {
		return executor.execute(
				new AddAllEESSandDisponCombusAndPrecioCombus(estacionDeServicios));
	}

	/**
	 * Actualiza una estación de servicio. Se da por hecho que en la estación de servicio
	 * ya existe en la base de datos y contiene todos los datos ya validados y es completo.
	 * <p>
	 * Solo se ignoran los campos de extCode y disponibilidad de combustibles.
	 *
	 * @param estacionDeServicio Estación de servicio a actualizar con todos los datos necesarios.
	 * @return Estación de servicio actualizada. Null en caso de fallo.
	 */
	@Override
	public EstacionDeServicio updateEESS(EstacionDeServicio estacionDeServicio) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Optional<EstacionDeServicio> findEESSById(String id) {
		return executor.execute(new FindEESSById(id));
	}

	@Override
	public Optional<EstacionDeServicio> findEESSByExtCode(int extCodeId) {
		return executor.execute(new FindEESSByExtCodeId(extCodeId));
	}


	@Override
	public List<EstacionDeServicio> findAllEESS() {
		return executor.execute(new FindAllEESS());
	}

	/**
	 * Obtiene todos los combustibles disponibles para un conjunto de estaciones de servicio.
	 *
	 * @param eessIds Conjunto de IDs de estaciones de servicio.
	 * @return Lista de DTOs de combustibles disponibles.
	 */
	@Override
	public Collection<CombustibleDisponibleDTO> findAllCombustiblesDisponiblesByEESSCollectionIds(
			Collection<Integer> eessIds) {
		return executor.execute(new FindAllCombustiblesDisponiblesByEESSCollectionIds(eessIds));
	}
}
