package com.inggarciabaldo.carburo.application.persistance;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz común para un gateway que proporciona operaciones básicas
 * para cualquier gateway. Es decir, operaciones CRUD: añadir, eliminar,
 * actualizar, buscar por id y buscar todos.
 *
 * @param <T>
 */
public interface Gateway<T> {

	/**
	 * Añade un nuevo elemento a la tabla.
	 *
	 * @param t nuevo elemento
	 */
	Long add(T t) throws PersistenceException;

		/**
	 * Actualiza una fila.
	 *
	 * @param t datos nuevos que sobrescribirán a los anteriores
	 */
	void update(T t) throws PersistenceException;

	/**
	 * Busca una fila en la tabla.
	 *
	 * @param id clave primaria del registro a obtener
	 * @return un Optional que contiene el DTO del registro, o vacío si no existe
	 */
	Optional<T> findById(String id) throws PersistenceException;

	/**
	 * Recupera todos los datos de una tabla.
	 *
	 * @return lista con todos los registros
	 */
	List<T> findAll() throws PersistenceException;
}
