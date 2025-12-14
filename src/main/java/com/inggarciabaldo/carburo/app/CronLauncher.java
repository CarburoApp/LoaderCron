package com.inggarciabaldo.carburo.app;

import com.inggarciabaldo.carburo.config.persistencia.jdbc.Jdbc;
import com.inggarciabaldo.carburo.scheduler.QuartzScheduler;
import com.inggarciabaldo.carburo.util.log.Loggers;
import org.quartz.SchedulerException;
import org.slf4j.Logger;


public class CronLauncher {

	// cronLogger específico para el scheduler
	private static final Logger appLogger = Loggers.CRON;
	private static final Logger cronLogger = Loggers.CRON;

	public static void main(String[] args) {
		appLogger.info("=== Iniciando aplicación Carburo-Cron ===");

		// Probar conexión a la BD
		boolean ok = Jdbc.testConnection();
		if (ok) {
			appLogger.info("Conexión a la base de datos OK.");
		} else {
			appLogger.error(
					"Error al probar la conexión con la base de datos. Se procederá a finalizar la aplicación.");
			return;
		}

		try {
			QuartzScheduler quartzScheduler = new QuartzScheduler();
			quartzScheduler.iniciar();

			//Lanzamos inicialmente la aplicación para que cargue todos los datos inicialmente.
			appLogger.info("Ejecución del Job inmediatamente tras iniciar el scheduler.");
			quartzScheduler.ejecutarJobManual();

		} catch (SchedulerException e) {
			cronLogger.error("Error al iniciar el scheduler: {}", e.getMessage(), e);
		} catch (Exception e) {
			appLogger.error("Error inesperado en la aplicación: {}", e.getMessage(), e);
		}

		appLogger.info("=== Aplicación//CRON iniciado y configurado correctamente. ===");
	}
}
