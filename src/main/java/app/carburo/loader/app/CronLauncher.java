package app.carburo.loader.app;

import app.carburo.loader.config.persistencia.jdbc.Jdbc;
import app.carburo.loader.scheduler.QuartzScheduler;
import app.carburo.loader.util.log.Loggers;
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
		} catch (SchedulerException e) {
			cronLogger.error("Error al iniciar el scheduler: {}", e.getMessage(), e);
		} catch (Exception e) {
			appLogger.error("Error inesperado en la aplicación: {}", e.getMessage(), e);
		}

		appLogger.info("=== Aplicación//CRON iniciado y configurado correctamente. ===");
	}
}
