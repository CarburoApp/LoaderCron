package com.inggarciabaldo.carburo.scheduler.jobs;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Clase para gestionar los datos referentes a estadisticas de ejecución del cron.
 */
@Getter
@Setter
public class DatoDeEjecucion {

	// Archivos
	protected File jsonRespuestaArchivo;
	// Fechas de ejecución
	protected LocalDateTime fechaInicioEjecucion = LocalDateTime.now();
	protected LocalDateTime fechaFinEjecucion = LocalDateTime.now();
	protected LocalDate fechaDeParser = LocalDate.now();
	// Tiempos en milisegundos
	protected long tiempoPeticionApiMs = 0;
	protected long tiempoJSONParseoDTOMs = 0;
	protected long tiempoDTOParseoEntidades = 0;
	protected long tiempoParseoEESSMs = 0;
	protected long tiempoCargaDatosInicialesCache = 0;
	protected long tiempoObtenerEESSBD = 0;
	protected long tiempoPersistenciaEESSPrecioYDisponibilidadFueraDeBD = 0;
	protected long tiempoProcesamientoEESSRequierenActualizacion = 0;
	protected long tiempoProcesamientoDisponibilidadDeCombustiblesAInsertar = 0;
	protected long tiempoProcesamientoPreciosDeCombustiblesAInsertar = 0;
	protected long tiempoProcesamientoPreciosDeCombustiblesAActualizar = 0;
	protected long tiempoPersistenciaEESSActualizacion = 0;
	protected long tiempoPersistenciaInsertarDisponibilidadDeCombustibles = 0;
	protected long tiempoPersistenciaInsertarPreciosDeCombustibles = 0;
	protected long tiempoPersistenciaActualizarPreciosDeCombustibles = 0;
	protected long tiempoPersistenciaMs = 0;
	protected long tiempoTotalCron = 0;
	// Estadísticas de ejecución en computos totales
	protected int totalEESSEnJson = 0; //JSON
	protected int totalEESSEnDTO = 0; //DTO
	protected int totalEESSParseadas = 0;
	protected int totalEESSNoParseadasConErrores = 0;
	protected int totalEESSEnBDAlInicio = 0; // pre-BD
	protected int totalEESSParseadasFueraDeBD = 0;
	protected int totalEESSParseadasEnBD = 0;
	protected int totalEESSParseadasRequierenActualizacion = 0;
	protected int totalEESSPrecioYDisponibilidadFueraDeBDInsertadas = 0;
	protected int totalEESSInsertadas = 0;
	protected int totalEESSActualizadas = 0;
	protected int totalProcesamientoDisponibilidadAInsertar = 0;
	protected int totalPersistenciaDisponibilidad = 0;
	protected int totalProcesamientoPreciosAInsertar = 0;
	protected int totalPersistenciaInsertarPrecios = 0;
	protected int totalProcesamientoPreciosAActualizar = 0;
	protected int totalPersistenciaActualizarPrecios = 0;
	protected int totalPreciosEnDTO = 0;
	protected int totalPreciosParseadas = 0;
	protected int totalPreciosInsertados = 0;
	protected int totalPreciosActualizados = 0;

	protected DatoDeEjecucion() {}

	/*
	 * Métodos auxiliares
	 */

	public void incrementarTotalErroresParseEESS() {totalEESSNoParseadasConErrores++;}

	protected String getInformeEjecucion() {

		return "\n" + "============================================================\n" +
				"                     RESUMEN EJECUCIÓN CRON\n" +
				"============================================================\n" +
				"FECHAS\n" +
				"------------------------------------------------------------\n" +
				"Inicio ejecución : " + formatoHora(fechaInicioEjecucion) + "\n" +
				"Fin ejecución    : " + formatoHora(fechaFinEjecucion) + "\n" +
				"Duración total   : " + formatoTiempo(tiempoTotalCron) + "\n\n" +
				"DATOS RECUPERADOS\n" +
				"------------------------------------------------------------\n" +
				"EESS en JSON                 : " + totalEESSEnJson + "\n" +
				"EESS en DTO                  : " + totalEESSEnDTO + "\n" +
				"Precios en DTO               : " + totalPreciosEnDTO + "\n\n" +
				"PROCESAMIENTO EESS\n" +
				"------------------------------------------------------------\n" +
				"EESS parseadas correctamente           : " + totalEESSParseadas +
				" (" + String.format("%.2f", getPorcentajeParseoEESS()) + "%)\n" +
				"EESS con errores de parseo             : " +
				totalEESSNoParseadasConErrores + "\n" +
				"EESS parseadas fuera de BD             : " +
				totalEESSParseadasFueraDeBD + "\n" +
				"EESS parseadas en BD                   : " +
				totalEESSParseadasEnBD + "\n" +
				"EESS que requieren actualización      : " +
				totalEESSParseadasRequierenActualizacion + "\n\n" +
				"PERSISTENCIA EESS\n" +
				"------------------------------------------------------------\n" +
				"EESS insertadas                        : " +
				totalEESSInsertadas + "\n" +
				"EESS actualizadas                      : " +
				totalEESSActualizadas + "\n" +
				"TOTAL EESS persistidas                 : " +
				(totalEESSInsertadas + totalEESSActualizadas) + "\n" +
				"EESS Precio/Disponibilidad fuera BD    : " +
				totalEESSPrecioYDisponibilidadFueraDeBDInsertadas + "\n\n" +
				"PROCESAMIENTO PRECIOS\n" +
				"------------------------------------------------------------\n" +
				"Precios a insertar                     : " +
				totalProcesamientoPreciosAInsertar + "\n" +
				"Precios insertados                      : " +
				totalPreciosInsertados + "\n" +
				"Precios a actualizar                     : " +
				totalProcesamientoPreciosAActualizar + "\n" +
				"Precios actualizados                     : " +
				totalPreciosActualizados + "\n" +
				"TOTAL precios persistidos                : " +
				(totalPreciosInsertados + totalPreciosActualizados) + "\n\n" +
				"TIEMPOS DE EJECUCIÓN\n" +
				"------------------------------------------------------------\n" +
				"Petición API                            : " +
				formatoTiempo(tiempoPeticionApiMs) + "\n" +
				"Parseo JSON → DTO                       : " +
				formatoTiempo(tiempoJSONParseoDTOMs) + "\n" +
				"Parseo DTO → Entidad                     : " +
				formatoTiempo(tiempoDTOParseoEntidades) + "\n" +
				"Parseo EESS                             : " +
				formatoTiempo(tiempoParseoEESSMs) + "\n" +
				"Persistencia EESS y Precios (total)     : " +
				formatoTiempo(tiempoPersistenciaMs) + "\n" +
				"============================================================\n";
	}


	protected double getPorcentajeParseoEESS() {
		if (totalEESSEnDTO == 0) return 0;
		return (totalEESSParseadas * 100.0) / totalEESSEnDTO;
	}


	/**
	 * Formatea milisegundos a minutos y segundos legibles
	 */
	private String formatoTiempo(long millis) {
		Duration duration = Duration.ofMillis(millis);
		long minutos = duration.toMinutes();
		long segundos = duration.minusMinutes(minutos).getSeconds();
		long milisegundos = millis % 1000;
		return String.format("%dm %ds %dms", minutos, segundos, milisegundos);
	}

	/**
	 * Formatea hora legible
	 */
	protected String formatoHora(LocalDateTime fecha) {
		return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
	}


}
