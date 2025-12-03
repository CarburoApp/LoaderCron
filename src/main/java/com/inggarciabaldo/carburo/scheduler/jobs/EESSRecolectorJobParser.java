package com.inggarciabaldo.carburo.scheduler.jobs;

import com.inggarciabaldo.carburo.util.log.Loggers;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Job de Quartz que se ejecuta periódicamente para:
 * 1. Obtener todas las estaciones mediante GasStationHttpRequest.
 * 2. Parsear cada EESS y sus precios usando {@link Parser#parseAll(JSONObject)}
 * 3. Persistir en la base de datos usando JPA.
 * - Registra tiempos y genera resumen final
 * <p>
 * La ejecución está protegida para que cualquier error no afecte al cron.
 */
public class EESSRecolectorJobParser implements Job {

	// Logger específico para la aplicación y para el cron
	private static final Logger loggerApp = Loggers.GENERAL;
	private static final Logger loggerCron = Loggers.CRON;


	// Utilidades
	//	private final GasStationHttpRequest request = new GasStationHttpRequest();
	//	private final PropertyLoader propertyLoader = PropertyLoader.getInstance();
	//	private final Parser parser = new Parser();

	@Override
	public void execute(JobExecutionContext context) {
		LocalDateTime inicioEjecucion = LocalDateTime.now();
		loggerApp.info("<<< Activación del CRON >>> Hora de inicio: {}",
					   formatoHora(inicioEjecucion));

		// 1. Petición API
		long tiempoPeticionMs;
		JSONObject jsonResAPI;
		{
			long inicioPeticion = System.currentTimeMillis();
			jsonResAPI       = obtenerJSONEstaciones();
			tiempoPeticionMs = System.currentTimeMillis() - inicioPeticion;
		}

		if (jsonResAPI == null || jsonResAPI.isEmpty()) {
			loggerCron.error(
					"CRON anulado: fallo al recuperar estaciones desde la API o lista vacía.");
			return;
		}

		//		int totalEESSJson = jsonResAPI.optJSONArray(
		//				propertyLoader.getJsonKeyProperty("lista.eess.precio")).length();

		// 2. Parseo
		long tiempoParseoMs;
		//List<ES> ESParseados;
		//		{
		//			long inicioParseo = System.currentTimeMillis();
		//			ESParseados    = parser.parseAll(jsonResAPI);
		//			tiempoParseoMs = System.currentTimeMillis() - inicioParseo;
		//		}
		//
		//		int totalParseados = ESParseados.size();
		//
		//		if (totalParseados == 0) {
		//			loggerCron.warn(
		//					"No se parseó ninguna estación correctamente. CRON finalizado.");
		//			return;
		//		}
		//
		//		// 3. Persistencia
		//		long tiempoPersistenciaMs;
		//		{
		//			long inicioPersistencia = System.currentTimeMillis();
		//			persistirBatch(ESParseados);
		//			tiempoPersistenciaMs = System.currentTimeMillis() - inicioPersistencia;
		//		}
		//
		//		LocalDateTime finEjecucion = LocalDateTime.now();
		//
		//		// 4. Resumen final
		//		String resumenCron = "<<< Resumen ejecución CRON >>>\n" + "Hora de inicio: " +
		//				formatoHora(inicioEjecucion) + ", Hora de fin: " +
		//				formatoHora(finEjecucion) + "\n" + "Total EESS recuperadas en JSON: " +
		//				totalEESSJson + "\n" + "Total EESS parseadas correctamente: " +
		//				totalParseados + "\n" + "Tiempo de petición API: " +
		//				formatoTiempo(tiempoPeticionMs) + "\n" + "Tiempo de parseo: " +
		//				formatoTiempo(tiempoParseoMs) + "\n" + "Tiempo de persistencia: " +
		//				formatoTiempo(tiempoPersistenciaMs) + "\n";
		//
		//		loggerCron.info(resumenCron);

	}

	/**
	 * Obtiene el JSON de estaciones desde la API
	 */
	private JSONObject obtenerJSONEstaciones() {
		try {
			return null;//request.getAllStations();
		} catch (Exception e) {
			loggerCron.error("Error al obtener JSON de estaciones: {}", e.getMessage(),
							 e);
			return null;
		}
	}

	/**
	 * Persiste todas las EESS en una única sesión JPA
	 */
	//	private void persistirBatch(List<ES> listaES) {
	//		// ===========================
	//		// Configuración de batch y multihilo
	//		// ===========================
	//		int numHilos = 4;       // número de hilos concurrentes, seguro para Supabase/Postgres
	//		int batchSize = 100;    // flush/clear cada 100 entidades
	//		int chunkSize = 200;    // tamaño de cada chunk que procesa un hilo
	//
	//		ExecutorService executor = Executors.newFixedThreadPool(numHilos);
	//		List<Future<?>> futures = new ArrayList<>();
	//
	//		// ===========================
	//		// Dividir la lista en chunks
	//		// ===========================
	//		for (int i = 0; i < listaES.size(); i += chunkSize) {
	//			int from = i;
	//			int to = Math.min(i + chunkSize, listaES.size());
	//			// Crear copia para evitar problemas con subList concurrente
	//			List<ES> chunk = new ArrayList<>(listaES.subList(from, to));
	//
	//			// ===========================
	//			// Procesar cada chunk en un hilo
	//			// ===========================
	//			futures.add(executor.submit(() -> {
	//				EntityManager em = JPAUtil.getEntityManager();
	//				EntityTransaction tx = null;
	//
	//				try {
	//					tx = em.getTransaction();
	//					tx.begin();
	//					int count = 0;
	//
	//					for (ES ES : chunk) {
	//						// Persistir o actualizar según corresponda
	//						if (ES.getId() == null) {
	//							em.persist(ES);
	//						} else {
	//							em.merge(ES);
	//						}
	//
	//						// Flush y clear por batch
	//						if (++count % batchSize == 0) {
	//							em.flush();
	//							em.clear(); // cuidado: relaciones desasociadas
	//						}
	//
	//						// Opcional: persistencia de precios si se requiere
	//						// for (PrecioCombustible precio : eess.getPrecios()) {
	//						//     if (precio.getId() == null) em.persist(precio);
	//						//     else em.merge(precio);
	//						// }
	//					}
	//
	//					tx.commit();
	//					loggerCron.info(
	//							"Chunk persistido correctamente. Estaciones procesadas en este hilo: {}",
	//							chunk.size());
	//
	//				} catch (Exception e) {
	//					if (tx != null && tx.isActive()) tx.rollback();
	//					loggerCron.error("Error persistiendo chunk. Transacción abortada: {}",
	//									 e.getMessage(), e);
	//
	//				} finally {
	//					if (em.isOpen()) em.close(); // cierre seguro del EntityManager
	//				}
	//			}));
	//		}
	//
	//		// ===========================
	//		// Esperar a que todos los hilos terminen
	//		// ===========================
	//		for (Future<?> f : futures) {
	//			try {
	//				f.get(); // bloquea hasta que termine cada hilo
	//			} catch (InterruptedException | ExecutionException e) {
	//				loggerCron.error("Error en hilo de persistencia: {}", e.getMessage(), e);
	//			}
	//		}
	//
	//		executor.shutdown();
	//		loggerCron.info("Persistencia multihilo completada. Total estaciones: {}",
	//						listaES.size());
	//	}


	/**
	 * Formatea milisegundos a minutos y segundos legibles
	 */
	private String formatoTiempo(long millis) {
		Duration duration = Duration.ofMillis(millis);
		long minutos = duration.toMinutes();
		long segundos = duration.minusMinutes(minutos).getSeconds();
		return String.format("%dm %ds", minutos, segundos);
	}

	/**
	 * Formatea hora legible
	 */
	private String formatoHora(LocalDateTime fecha) {
		return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
	}
}