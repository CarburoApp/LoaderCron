package com.inggarciabaldo.carburo.scheduler.jobs;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.time.Duration;
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
	// Tiempos en milisegundos
	protected long tiempoPeticionApiMs = 0;
	protected long tiempoJSONParseoDTOMs = 0;
	protected long tiempoDTOParseoEntidades = 0;
	protected long tiempoParseoEESSMs = 0;
	protected long tiempoCargaDatosInicialesCache = 0;
	protected long tiempoObtenerEESSBD = 0;
	protected long tiempoPersistenciaEESSPrecioYDisponibilidadFueraDeBD = 0;
	protected long tiempoProcesamientoEESSRequierenActualizacion = 0;
	protected long tiempoPersistenciaEESSActualizacion = 0;
	protected long tiempoProcesamientoDisponibilidadDeCombustiblesAInsertar = 0;
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

		String sb =
				"\n" + "============================================================\n" +
						"                RESUMEN EJECUCIÓN CRON\n" +
						"============================================================\n" +
						"FECHAS\n" +
						"------------------------------------------------------------\n" +
						"Inicio ejecución : " + formatoHora(fechaInicioEjecucion) + "\n" +
						"Fin ejecución    : " + formatoHora(fechaFinEjecucion) + "\n" +
						"Duración total   : " + formatoTiempo(tiempoTotalCron) + "\n\n" +
						"DATOS RECUPERADOS\n" +
						"------------------------------------------------------------\n" +
						"EESS en JSON     : " + totalEESSEnJson + "\n" +
						"EESS en DTO      : " + totalEESSEnDTO + "\n" +
						"Precios en DTO   : " + totalPreciosEnDTO + "\n\n" +
						"PROCESADO\n" +
						"------------------------------------------------------------\n" +
						"EESS parseadas correctamente : " + totalEESSParseadas + " (" +
						String.format("%.2f", getPorcentajeParseoEESS()) + "%)\n\n" +
						"PERSISTENCIA\n" +
						"------------------------------------------------------------\n" +
						"EESS insertadas      : " + totalEESSInsertadas + "\n" +
						"EESS actualizadas    : " + totalEESSActualizadas + "\n" +
						"TOTAL EESS persistidas: " +
						(totalEESSInsertadas + totalEESSActualizadas) + "\n\n" +
						"Precios insertados   : " + totalPreciosInsertados + "\n" +
						"Precios actualizados : " + totalPreciosActualizados + "\n" +
						"TOTAL precios persistidos: " +
						(totalPreciosInsertados + totalPreciosActualizados) + "\n\n" +
						"TIEMPOS DE EJECUCIÓN\n" +
						"------------------------------------------------------------\n" +
						"Petición API         : " + formatoTiempo(tiempoPeticionApiMs) +
						"\n" + "Parseo JSON → DTO    : " +
						formatoTiempo(tiempoJSONParseoDTOMs) + "\n" +
						"Parseo DTO → Entity  : " + formatoTiempo(tiempoParseoEESSMs) +
						"\n" + "Persistencia BD      : " +
						formatoTiempo(tiempoPersistenciaMs) + "\n" +
						"============================================================\n";

		return sb;
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
