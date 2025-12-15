package com.inggarciabaldo.carburo.application.service.eess.crud.commands;

import com.inggarciabaldo.carburo.application.Factorias;
import com.inggarciabaldo.carburo.application.model.Combustible;
import com.inggarciabaldo.carburo.application.model.EstacionDeServicio;
import com.inggarciabaldo.carburo.application.model.PrecioCombustible;
import com.inggarciabaldo.carburo.application.persistance.combustibleDisponible.CombustibleDisponibleGateway;
import com.inggarciabaldo.carburo.application.persistance.combustibleDisponible.CombustibleDisponibleGateway.CombustibleDisponibleRecord;
import com.inggarciabaldo.carburo.application.persistance.eess.EESSGateway;
import com.inggarciabaldo.carburo.application.persistance.eess.EESSGateway.EESSRecord;
import com.inggarciabaldo.carburo.application.persistance.precioCombustible.PrecioCombustibleGateway;
import com.inggarciabaldo.carburo.application.persistance.precioCombustible.PrecioCombustibleGateway.PrecioCombustibleRecord;
import com.inggarciabaldo.carburo.application.service.eess.crud.EntityAssembler;
import com.inggarciabaldo.carburo.application.service.util.command.Command;
import com.inggarciabaldo.carburo.util.log.Loggers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Comando encargado de realizar la INSERCIÓN MASIVA de:
 * <ul>
 *     <li>Estaciones de Servicio (EESS)</li>
 *     <li>Disponibilidad de combustibles</li>
 *     <li>Precios de combustibles</li>
 * </ul>
 *
 * <p>
 * Se asume que los datos recibidos son correctos y no contienen duplicados.
 * No se realizan comprobaciones previas contra la base de datos.
 * </p>
 */
public class AddAllEESSandDisponCombusAndPrecioCombus implements Command<Integer> {

	private final Collection<EstacionDeServicio> estacionesDeServicio;

	public AddAllEESSandDisponCombusAndPrecioCombus(
			Collection<EstacionDeServicio> estacionesDeServicio) {

		if (estacionesDeServicio == null) throw new IllegalArgumentException(
				"La colección de Estaciones de Servicio no puede ser nula.");

		this.estacionesDeServicio = estacionesDeServicio;
	}

	/**
	 * Ejecuta el proceso completo de inserción masiva.
	 *
	 * @return número total de Estaciones de Servicio insertadas
	 */
	@Override
	public Integer execute() {

		logInicioProceso();

		if (estacionesDeServicio.isEmpty()) {
			logColeccionVacia();
			return 0;
		}

		// Gateways de persistencia
		EESSGateway eessGateway = Factorias.persistence.forEESS();
		CombustibleDisponibleGateway combDispGateway = Factorias.persistence.forCombustibleDisponible();
		PrecioCombustibleGateway precioCombGateway = Factorias.persistence.forPrecioCombustible();

		// Transformación de entidades a records
		Collection<EESSRecord> eessRecords = new ArrayList<>();
		Collection<CombustibleDisponibleRecord> combDispRecords = new ArrayList<>();
		Collection<PrecioCombustibleRecord> precioRecords = new ArrayList<>();

		/*
		 * Transforma las entidades EESS de dominio en records de persistencia. Por motivos de
		 * integridad referencial debe de hacerse primero las EESS, y luego de añadirlas se
		 * obtienen sus IDs para crear los registros de disponibilidad y
		 */
		// EESS
		for (EstacionDeServicio eess : estacionesDeServicio)
			eessRecords.add(EntityAssembler.toRecord(eess));

		// Inserciones masivas
		Collection<EESSRecord> eessInsertadas = insertarEESS(eessGateway, eessRecords);

		// Asignar IDs generadas a los records de EESS para crear las relaciones correctamente
		// Map<extCode, idBD>
		Map<Integer, Integer> idPorExtCode = eessInsertadas.stream()
				.collect(Collectors.toMap(r -> r.extCode, r -> r.id));

		// Asignamos el ID a cada EESS de Dominio
		estacionesDeServicio.forEach(eess -> {
			eess.setId(idPorExtCode.get(eess.getExtCode()));
		});

		// Se han de insertar antes las EESS para poder obtener sus IDs
		transformarEntidadesARecords(combDispRecords, precioRecords);

		logResultadoTransformacion(eessRecords.size(), combDispRecords.size(),
								   precioRecords.size());

		// Inserciones masivas del resto de entidades
		insertarDisponibilidadCombustible(combDispGateway, combDispRecords);

		insertarPreciosCombustible(precioCombGateway, precioRecords);

		return idPorExtCode.size();
	}

	/* ======================================================
	 * =============== MÉTODOS PRIVADOS =====================
	 * ====================================================== */

	/**
	 * Log de inicio del proceso.
	 */
	private void logInicioProceso() {
		Loggers.DB.info(
				"COMIENZA el proceso de INSERCIÓN MASIVA de EESS con disponibilidad y precios. Total EESS: {}.",
				estacionesDeServicio.size());
	}

	/**
	 * Log cuando la colección de EESS está vacía.
	 */
	private void logColeccionVacia() {
		Loggers.DB.info(
				"FINALIZA el proceso de INSERCIÓN MASIVA de EESS. COLECCIÓN VACÍA.");
	}

	/**
	 * Transforma las entidades de dominio en records de persistencia.
	 */
	private void transformarEntidadesARecords(
			Collection<CombustibleDisponibleRecord> combDispRecords,
			Collection<PrecioCombustibleRecord> precioRecords) {

		for (EstacionDeServicio eess : estacionesDeServicio) {
			// Disponibilidad de combustibles
			for (Combustible combustible : eess.getCombustiblesDisponibles()) {
				CombustibleDisponibleRecord record = new CombustibleDisponibleRecord();
				record.idEESS        = eess.getId();
				record.idCombustible = combustible.getId();
				combDispRecords.add(record);
			}

			// Precios de combustibles
			for (PrecioCombustible precio : eess.getPreciosCombustibles()) {
				precioRecords.add(EntityAssembler.toPrecioCombustibleRecord(precio));
			}
		}
	}

	/**
	 * Log del resultado de la transformación a records.
	 */
	private void logResultadoTransformacion(int totalEESS, int totalCombDisp,
											int totalPrecios) {

		Loggers.DB.info(
				"Transformadas {} EESS a {} records EESS, {} records Combustible-Disponible y {} records Precio-Combustible.",
				estacionesDeServicio.size(), totalEESS, totalCombDisp, totalPrecios);
	}

	/**
	 * Inserta masivamente las Estaciones de Servicio.
	 *
	 * @return la coleccion de eess insertadas con sus IDs asignadas.
	 */
	private Collection<EESSRecord> insertarEESS(EESSGateway gateway,
												Collection<EESSRecord> records) {

		Loggers.DB.info("COMIENZO de INSERCIÓN MASIVA de EESS. Pendientes: {}.",
						records.size());

		long inicio = System.currentTimeMillis();
		//la llamada a addAll devuelve las estaciones insertadas para poder asignar las ids a las entidades.
		Collection<EESSRecord> insertadas = gateway.addAll(records);

		Loggers.DB.info("INSERTADAS {} de {} EESS en {} ms.", insertadas.size(),
						records.size(), System.currentTimeMillis() - inicio);

		return insertadas;
	}

	/**
	 * Inserta masivamente la disponibilidad de combustibles.
	 */
	private void insertarDisponibilidadCombustible(CombustibleDisponibleGateway gateway,
												   Collection<CombustibleDisponibleRecord> records) {

		Loggers.DB.info(
				"COMIENZO de INSERCIÓN MASIVA de Combustible-Disponible. Pendientes: {}.",
				records.size());

		long inicio = System.currentTimeMillis();
		int insertadas = gateway.addAll(records);

		Loggers.DB.info("INSERTADOS {} de {} Combustible-Disponible en {} ms.",
						insertadas, records.size(), System.currentTimeMillis() - inicio);
	}

	/**
	 * Inserta masivamente los precios de combustibles.
	 */
	private void insertarPreciosCombustible(PrecioCombustibleGateway gateway,
											Collection<PrecioCombustibleRecord> records) {

		Loggers.DB.info(
				"COMIENZO de INSERCIÓN MASIVA de Precio-Combustible. Pendientes: {}.",
				records.size());

		long inicio = System.currentTimeMillis();
		int insertadas = gateway.addAll(records);

		Loggers.DB.info("INSERTADOS {} de {} Precio-Combustible en {} ms.", insertadas,
						records.size(), System.currentTimeMillis() - inicio);
	}
}
