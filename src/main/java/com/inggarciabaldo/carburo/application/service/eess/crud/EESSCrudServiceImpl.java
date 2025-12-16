package com.inggarciabaldo.carburo.application.service.eess.crud;

import com.inggarciabaldo.carburo.application.model.EstacionDeServicio;
import com.inggarciabaldo.carburo.application.model.PrecioCombustible;
import com.inggarciabaldo.carburo.application.service.eess.EESSCrudService;
import com.inggarciabaldo.carburo.application.service.eess.crud.commands.*;
import com.inggarciabaldo.carburo.application.service.util.command.executor.JdbcCommandExecutor;
import com.inggarciabaldo.carburo.application.service.util.dto.CombustibleDisponibleDTO;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
	 * Inserta masivamente una colección de combustibles disponibles para estaciones de servicio.
	 *
	 * @param combustibleDisponibleDTOs Colección de DTOs de combustibles disponibles a insertar.
	 * @return Número de combustibles disponibles insertados satisfactoriamente.
	 */
	@Override
	public int addAllCombustiblesDisponibles(
			Collection<CombustibleDisponibleDTO> combustibleDisponibleDTOs) {
		return executor.execute(
				new AddAllCombustiblesDisponibles(combustibleDisponibleDTOs));
	}

	/**
	 * Inserta masivamente una colección de precios de combustibles.
	 *
	 * @param precioCombustibles Colección de precios de combustibles a insertar.
	 * @return Número de precios de combustibles insertados satisfactoriamente.
	 */
	@Override
	public int addAllPrecioCombustibles(
			Collection<PrecioCombustible> precioCombustibles) {
		return executor.execute(new AddAllPrecioCombustibles(precioCombustibles));
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
		return executor.execute(new UpdateEESS(estacionDeServicio));
	}

	/**
	 * Actualiza masivamente una colección de precios de combustibles.
	 *
	 * @param precioCombustibles Colección de precios de combustibles a insertar.
	 * @return Número de precios de combustibles insertados satisfactoriamente.
	 */
	@Override
	public int updateAllPrecioCombustibles(
			Collection<PrecioCombustible> precioCombustibles) {
		return executor.execute(new UpdateAllPrecioCombustibles(precioCombustibles));
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
		return executor.execute(
				new FindAllCombustiblesDisponiblesByEESSCollectionIds(eessIds));
	}

	/**
	 * Obtiene todos los precios de combustibles para un conjunto de estaciones de servicio en una fecha concreta (diaria).
	 *
	 * @param eessIds Conjunto de IDs de estaciones de servicio.
	 * @param fecha   {@link LocalDate} fecha a nivel de día para la que se obtendrán los registros.
	 * @return {@link Collection} de DTOs de precios de combustibles en la BD para eeess y fecha dada.
	 */
	@Override
	public Collection<PrecioCombustible> findAllPrecioCombustibleByEESSCollectionIdsAndFecha(
			Map<Integer, EstacionDeServicio> eessIds, LocalDate fecha) {
		return executor.execute(new FindAllPrecioCombustibleByEESSCollectionIdsAndFecha(eessIds, fecha));
	}
}
