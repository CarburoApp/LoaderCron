package com.inggarciabaldo.carburo.application.persistance.combustibledisponible;

import com.inggarciabaldo.carburo.application.persistance.PersistenceException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CombustibleDisponibleGateway {

	void add(short idCombustible, int idEESS) throws PersistenceException;

	/**
	 * Inserta masivamente una colección de objetos Combustible-Disponible.
	 *
	 * @param records colección de EESS a insertar
	 * @return número de objetos Combustible-Disponible insertados satisfactoriamente
	 * @throws PersistenceException si ocurre un error de persistencia
	 */
	int addAll(Collection<CombustibleDisponibleRecord> records);

	void remove(short idCombustible, int idEESS) throws PersistenceException;

	List<CombustibleDisponibleRecord> findAll() throws PersistenceException;

	Optional<CombustibleDisponibleRecord> findById(short idCombustible, int idEESS)
			throws PersistenceException;

	List<CombustibleDisponibleRecord> findByCombustible(short idCombustible)
			throws PersistenceException;

	List<CombustibleDisponibleRecord> findByEESS(int idEESS) throws PersistenceException;

	class CombustibleDisponibleRecord {
		public short idCombustible;
		public int idEESS;
	}
}
