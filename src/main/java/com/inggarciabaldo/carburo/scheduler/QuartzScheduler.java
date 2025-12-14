package com.inggarciabaldo.carburo.scheduler;

import com.inggarciabaldo.carburo.scheduler.jobs.EESSRecolectorJobParser;
import com.inggarciabaldo.carburo.util.log.Loggers;
import com.inggarciabaldo.carburo.util.properties.PropertyLoader;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;

import java.time.ZoneId;
import java.util.Date;

/**
 * QuartzScheduler gestiona la ejecución de jobs periódicos usando Quartz.
 * Configuración de intervalos y hora de inicio se leen desde application.properties
 * a través de PropertyLoader.
 */
public class QuartzScheduler {

	//Constantes de la clase
	private static final String JOB_NAME = "recolectorJob";
	private static final String TRIGGER_NAME = "recolectorTrigger";
	private static final String GROUP = "grupoCron";

	// Logger específico para el scheduler
	private static final Logger cronLogger = Loggers.CRON;

	// Configuración cargada desde properties
	private int intervaloMinutos;
	private int horaInicio;
	private int minutoInicio;
	private final ZoneId timeZone;

	// Scheduler de Quartz
	private Scheduler scheduler;

	/**
	 * Constructor que carga la configuración usando PropertyLoader
	 * y crea la instancia de Scheduler, **sin activarlo**.
	 */
	public QuartzScheduler() {
		PropertyLoader loader = PropertyLoader.getInstance();

		try {
			intervaloMinutos = Integer.parseInt(
					loader.getApplicationProperty("cron.interval.minutes"));
			horaInicio       = Integer.parseInt(
					loader.getApplicationProperty("cron.start.hour"));
			minutoInicio     = Integer.parseInt(
					loader.getApplicationProperty("cron.start.minute"));
			timeZone         = ZoneId.of(loader.getApplicationProperty("cron.timezone"));
		} catch (NumberFormatException | NullPointerException e) {
			throw new IllegalStateException(
					"La configuración del cron es inválida o incompleta.", e);
		}

		try {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			cronLogger.info(
					"Scheduler creado correctamente. Configuración cargada de las propiedades de la aplicación: inicio {}:{}, intervalo {} minutos, timezone {}",
					horaInicio, minutoInicio, intervaloMinutos, timeZone);
		} catch (SchedulerException e) {
			throw new IllegalStateException("Error al crear el scheduler.", e);
		}

	}

	/**
	 * Comprueba si el scheduler está corriendo
	 */
	public boolean isActivo() {
		try {
			return scheduler != null && !scheduler.isShutdown();
		} catch (Exception e) {
			cronLogger.error("Error comprobando estado del scheduler.", e);
			return false;
		}
	}

	/**
	 * Inicia el scheduler y programa el job RecolectorJob
	 */
	public void iniciar() throws SchedulerException {
		//Comprobamos si se ha lanzado ya.
		if (isActivo() && scheduler.isStarted()) {
			cronLogger.warn("Scheduler ya estaba iniciado.");
			return;
		}

		cronLogger.info("Iniciando scheduler...");
		scheduler.start();
		cronLogger.info("Scheduler iniciado.");

		// Crear job
		JobDetail job = JobBuilder.newJob(EESSRecolectorJobParser.class)
				.withIdentity(JOB_NAME, GROUP).build();
		cronLogger.info("Job '{}' creado.", JOB_NAME);

		// Crear trigger según la configuración del properties
		Trigger trigger = crearTrigger(horaInicio, minutoInicio, intervaloMinutos);
		scheduler.scheduleJob(job, trigger);
		cronLogger.info("Job programado: empieza a {}:{}, cada {} minutos (timezone {}).",
						horaInicio, minutoInicio, intervaloMinutos, timeZone);
	}


	/**
	 * Detiene el scheduler si está iniciado
	 */
	public void detener() throws SchedulerException {
		if (scheduler != null && !scheduler.isShutdown()) {
			scheduler.shutdown();
			cronLogger.info("Scheduler detenido.");
		} else {
			cronLogger.warn("Scheduler no estaba iniciado o ya estaba detenido.");
		}
	}


	/**
	 * Reinicia completamente el scheduler: lo detiene y lo vuelve a iniciar con la configuración original.
	 */
	public void reiniciar() throws SchedulerException {
		cronLogger.info("Reiniciando scheduler...");
		if (isActivo()) {
			detener();
		}
		iniciar();
		cronLogger.info("Scheduler reiniciado con la configuración original.");
	}

	/**
	 * Reprograma el job con nueva configuración pasada por parámetros.
	 * Si el scheduler no está activo, solo actualiza la configuración para cuando se inicie.
	 *
	 * @param nuevaHora      hora de inicio (0-23)
	 * @param nuevoMinuto    minuto de inicio (0-59)
	 * @param nuevoIntervalo intervalo en minutos (>0)
	 * @throws SchedulerException si hay problemas al reprogramar
	 */
	public void reprogramar(int nuevaHora, int nuevoMinuto, int nuevoIntervalo)
			throws SchedulerException {
		// Validaciones de parámetros
		if (nuevaHora < 0 || nuevaHora > 23)
			throw new IllegalArgumentException("Hora inválida: " + nuevaHora);
		if (nuevoMinuto < 0 || nuevoMinuto > 59)
			throw new IllegalArgumentException("Minuto inválido: " + nuevoMinuto);
		if (nuevoIntervalo <= 0)
			throw new IllegalArgumentException("Intervalo inválido: " + nuevoIntervalo);

		// Actualizar configuración interna
		this.horaInicio       = nuevaHora;
		this.minutoInicio     = nuevoMinuto;
		this.intervaloMinutos = nuevoIntervalo;

		// Crear trigger con la nueva configuración
		Trigger nuevoTrigger = crearTrigger(nuevaHora, nuevoMinuto, nuevoIntervalo);
		scheduler.rescheduleJob(new TriggerKey(TRIGGER_NAME, GROUP), nuevoTrigger);

		cronLogger.info(
				"Job reprogramado: empieza a {}:{}, cada {} minutos (timezone {}).",
				nuevaHora, nuevoMinuto, nuevoIntervalo, timeZone);
	}

	/**
	 * Ejecuta el job RecolectorJob manualmente, independiente del cron.
	 * El job se ejecuta inmediatamente y se elimina del scheduler tras su ejecución.
	 */
	public void ejecutarJobManual() {
		try {
			if (scheduler == null) {
				cronLogger.info(
						"Scheduler no inicializado. Creando scheduler para ejecución manual.");
				scheduler = StdSchedulerFactory.getDefaultScheduler();
				scheduler.start();
			}

			// Crear JobDetail temporal
			String jobManualName = "recolectorJob_manual_" + System.currentTimeMillis();
			JobDetail jobManual = JobBuilder.newJob(EESSRecolectorJobParser.class)
					.withIdentity(jobManualName, GROUP)
					.storeDurably(false) // no se guarda si no hay triggers permanentes
					.build();

			// Crear trigger inmediato, ejecuta solo una vez
			Trigger triggerManual = TriggerBuilder.newTrigger()
					.withIdentity("trigger_manual_" + System.currentTimeMillis(), GROUP)
					.startNow().withSchedule(
							SimpleScheduleBuilder.simpleSchedule().withRepeatCount(0))
					.build();

			// Programar y ejecutar
			scheduler.scheduleJob(jobManual, triggerManual);

			cronLogger.info("Ejecución manual de RecolectorJob disparada: {}",
							jobManualName);

			// Nota: Quartz automáticamente elimina el job si no es duradero y no tiene triggers permanentes
		} catch (SchedulerException e) {
			cronLogger.error("Error ejecutando RecolectorJob manualmente: {}",
							 e.getMessage(), e);
		}
	}


	/**
	 * Crea un trigger según los parámetros especificados
	 *
	 * @param horaInicio       hora de inicio
	 * @param minutoInicio     minuto de inicio
	 * @param intervaloMinutos intervalo en minutos
	 * @return Trigger configurado
	 */
	private Trigger crearTrigger(int horaInicio, int minutoInicio, int intervaloMinutos) {
		Date startAt = Date.from(java.time.LocalDateTime.now().withHour(horaInicio)
										 .withMinute(minutoInicio).withSecond(0)
										 .atZone(timeZone).toInstant());

		return TriggerBuilder.newTrigger().withIdentity(TRIGGER_NAME, GROUP)
				.startAt(startAt).withSchedule(SimpleScheduleBuilder.simpleSchedule()
													   .withIntervalInMinutes(
															   intervaloMinutos)
													   .repeatForever()).build();
	}
}
