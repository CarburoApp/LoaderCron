package com.inggarciabaldo.carburo.application.service.eess;

import com.inggarciabaldo.carburo.application.model.EstacionDeServicio;
import com.inggarciabaldo.carburo.application.model.PrecioCombustible;
import com.inggarciabaldo.carburo.application.service.util.dto.CombustibleDisponibleDTO;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EESSCrudService {

	EstacionDeServicio addEESS(EstacionDeServicio estacionDeServicio);

	/**
	 * Encargado de realizar la inserción masiva de estaciones de servicio, sus disponibilidades y precios de combustibles.
	 * Todos los datos se dan por comprobados, supiniendo que se ha verificado que no hay problemas de integridad referencial ni de duplicados.
	 *
	 * @return Número de estaciones de servicio insertadas satisfactoriamente.
	 */
	int addAllEESSandDisponCombusAndPrecioCombus(
			Collection<EstacionDeServicio> estacionDeServicios);

	/**
	 * Inserta masivamente una colección de combustibles disponibles para estaciones de servicio.
	 *
	 * @param combustibleDisponibleDTOs Colección de DTOs de combustibles disponibles a insertar.
	 * @return Número de combustibles disponibles insertados satisfactoriamente.
	 */
	int addAllCombustiblesDisponibles(
			Collection<CombustibleDisponibleDTO> combustibleDisponibleDTOs);

	/**
	 * Inserta masivamente una colección de precios de combustibles.
	 *
	 * @param precioCombustibles Colección de precios de combustibles a insertar.
	 * @return Número de precios de combustibles insertados satisfactoriamente.
	 */
	int addAllPrecioCombustibles(Collection<PrecioCombustible> precioCombustibles);

	/**
	 * Actualiza una estación de servicio. Se da por hecho que en la estación de servicio
	 * ya existe en la base de datos y contiene todos los datos ya validados y es completo.
	 * <p>
	 * Solo se ignoran los campos de extCode y disponibilidad de combustibles.
	 *
	 * @param estacionDeServicio Estación de servicio a actualizar con todos los datos necesarios.
	 * @return Estación de servicio actualizada. Null en caso de fallo.
	 */
	EstacionDeServicio updateEESS(EstacionDeServicio estacionDeServicio);

	/**
	 * Actualiza masivamente una colección de precios de combustibles.
	 *
	 * @param precioCombustibles Colección de precios de combustibles a insertar.
	 * @return Número de precios de combustibles insertados satisfactoriamente.
	 */
	int updateAllPrecioCombustibles(Collection<PrecioCombustible> precioCombustibles);

	Optional<EstacionDeServicio> findEESSById(String id);

	Optional<EstacionDeServicio> findEESSByExtCode(int extCodeId);

	/**
	 * Obtiene todas las estaciones de servicio.
	 *
	 * @return Lista de estaciones de servicio.
	 */
	List<EstacionDeServicio> findAllEESS();

	/**
	 * Obtiene todos los combustibles disponibles para un conjunto de estaciones de servicio.
	 *
	 * @param eessIds Conjunto de IDs de estaciones de servicio.
	 * @return {@link Collection} de DTOs de combustibles disponibles.
	 */
	Collection<CombustibleDisponibleDTO> findAllCombustiblesDisponiblesByEESSCollectionIds(
			Collection<Integer> eessIds);

	/**
	 * Obtiene todos los precios de combustibles para un conjunto de estaciones de servicio en una fecha concreta (diaria).
	 *
	 * @param eessIds Map de IDs y de estaciones de servicio.
	 * @param fecha   {@link LocalDate} fecha a nivel de día para la que se obtendrán los registros.
	 * @return {@link Collection} de DTOs de precios de combustibles en la BD para eeess y fecha dada.
	 */
	Collection<PrecioCombustible> findAllPrecioCombustibleByEESSCollectionIdsAndFecha(
			Map<Integer, EstacionDeServicio> eessIds, LocalDate fecha);
}
