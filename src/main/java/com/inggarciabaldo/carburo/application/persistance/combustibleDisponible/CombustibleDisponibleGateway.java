package com.inggarciabaldo.carburo.application.persistance.combustibleDisponible;

import com.inggarciabaldo.carburo.application.persistance.PersistenceException;

import java.util.List;
import java.util.Optional;

public interface CombustibleDisponibleGateway {

	void add(short idCombustible, int idEESS) throws PersistenceException;

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
