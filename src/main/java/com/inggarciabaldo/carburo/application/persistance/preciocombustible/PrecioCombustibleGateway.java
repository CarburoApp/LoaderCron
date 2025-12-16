package com.inggarciabaldo.carburo.application.persistance.preciocombustible;

import com.inggarciabaldo.carburo.application.persistance.PersistenceException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Gateway para la tabla PRECIOCOMBUSTIBLE.
 * Proporciona operaciones CRUD y métodos de búsqueda
 * por fecha, EESS, combustible y todas sus combinaciones.
 */
public interface PrecioCombustibleGateway {


	/**
	 * Inserta un nuevo registro de precio de combustible.
	 *
	 * @param precioCombustible registro a insertar
	 * @throws PersistenceException si ocurre un error de persistencia
	 */
	void add(PrecioCombustibleRecord precioCombustible) throws PersistenceException;

	/**
	 * Inserta masivamente una colección de nuevos registro de precio de combustible.
	 *
	 * @param coleccionPreciosCombustibles colección de registros a insertar
	 * @throws PersistenceException si ocurre un error de persistencia
	 */
	int addAll(Collection<PrecioCombustibleRecord> coleccionPreciosCombustibles);

	/**
	 * Actualiza un registro existente de precio de combustible
	 * identificado por su clave primaria.
	 *
	 * @param precioCombustible registro a actualizar
	 * @throws PersistenceException si ocurre un error de persistencia
	 */
	void update(PrecioCombustibleRecord precioCombustible) throws PersistenceException;

	/**
	 * Elimina un registro por su clave primaria.
	 *
	 * @param precioCombustible registro a eliminar (clave primaria)
	 * @throws PersistenceException si ocurre un error de persistencia
	 */
	void remove(PrecioCombustibleRecord precioCombustible) throws PersistenceException;

	/**
	 * Busca un registro por su clave primaria (id_combustible, id_eess, fecha).
	 */
	Optional<PrecioCombustibleRecord> findById(java.sql.Date fecha, int idEESS,
											   short idCombustible)
			throws PersistenceException;

	/**
	 * Obtiene todos los registros.
	 */
	List<PrecioCombustibleRecord> findAll() throws PersistenceException;

	/**
	 * Métodos de búsqueda por campo individual.
	 */
	List<PrecioCombustibleRecord> findByFecha(java.sql.Date fecha)
			throws PersistenceException;

	List<PrecioCombustibleRecord> findByEESS(int idEESS) throws PersistenceException;

	List<PrecioCombustibleRecord> findByCombustible(short idCombustible)
			throws PersistenceException;

	/**
	 * Métodos de búsqueda combinando dos campos.
	 */
	List<PrecioCombustibleRecord> findByFechaEESS(java.sql.Date fecha, int idEESS)
			throws PersistenceException;

	List<PrecioCombustibleRecord> findByFechaCombustible(java.sql.Date fecha,
														 short idCombustible)
			throws PersistenceException;

	List<PrecioCombustibleRecord> findByEESSCombustible(int idEESS, short idCombustible)
			throws PersistenceException;

	/**
	 * Registro de precio de combustible.
	 */
	class PrecioCombustibleRecord {
		public short idCombustible;
		public int idEESS;
		public java.sql.Date fecha;
		public Double precio;
	}
}
