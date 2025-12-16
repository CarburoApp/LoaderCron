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
		StringBuilder sb = new StringBuilder("\n").append(
						"============================================================\n")
				.append("                     RESUMEN EJECUCIÓN CRON\n")
				.append("============================================================\n")
				.append("FECHAS\n")
				.append("------------------------------------------------------------\n")
				.append("Inicio ejecución : ").append(formatoHora(fechaInicioEjecucion))
				.append("\n").append("Fin ejecución    : ")
				.append(formatoHora(fechaFinEjecucion)).append("\n")
				.append("Duración total   : ").append(formatoTiempo(tiempoTotalCron))
				.append("\n\n")

				.append("DATOS RECUPERADOS\n")
				.append("------------------------------------------------------------\n")
				.append("EESS en JSON                 : ").append(totalEESSEnJson)
				.append("\n").append("EESS en DTO                  : ")
				.append(totalEESSEnDTO).append("\n")
				.append("Precios en DTO               : ").append(totalPreciosEnDTO)
				.append("\n\n")

				.append("PROCESAMIENTO EESS\n")
				.append("------------------------------------------------------------\n")
				.append("EESS parseadas correctamente           : ")
				.append(totalEESSParseadas).append(" (")
				.append(String.format("%.2f", getPorcentajeParseoEESS())).append("%)\n")
				.append("EESS con errores de parseo             : ")
				.append(totalEESSNoParseadasConErrores).append("\n")
				.append("EESS parseadas fuera de BD             : ")
				.append(totalEESSParseadasFueraDeBD).append("\n")
				.append("EESS parseadas en BD                   : ")
				.append(totalEESSParseadasEnBD).append("\n")
				.append("EESS que requieren actualización      : ")
				.append(totalEESSParseadasRequierenActualizacion).append("\n\n")

				.append("PERSISTENCIA EESS\n")
				.append("------------------------------------------------------------\n")
				.append("EESS insertadas                        : ")
				.append(totalEESSInsertadas).append("\n")
				.append("EESS actualizadas                      : ")
				.append(totalEESSActualizadas).append("\n")
				.append("TOTAL EESS persistidas                 : ")
				.append(totalEESSInsertadas + totalEESSActualizadas).append("\n")
				.append("EESS Precio/Disponibilidad fuera BD    : ")
				.append(totalEESSPrecioYDisponibilidadFueraDeBDInsertadas).append("\n\n")

				.append("PROCESAMIENTO PRECIOS\n")
				.append("------------------------------------------------------------\n")
				.append("Precios a insertar                     : ")
				.append(totalProcesamientoPreciosAInsertar).append("\n")
				.append("Precios insertados                      : ")
				.append(totalPreciosInsertados).append("\n")
				.append("Precios a actualizar                     : ")
				.append(totalProcesamientoPreciosAActualizar).append("\n")
				.append("Precios actualizados                     : ")
				.append(totalPreciosActualizados).append("\n")
				.append("TOTAL precios persistidos                : ")
				.append(totalPreciosInsertados + totalPreciosActualizados).append("\n\n")

				.append("TIEMPOS DE EJECUCIÓN\n")
				.append("------------------------------------------------------------\n")
				.append("Petición API                            : ")
				.append(formatoTiempo(tiempoPeticionApiMs)).append("\n")
				.append("Parseo JSON → DTO                       : ")
				.append(formatoTiempo(tiempoJSONParseoDTOMs)).append("\n")
				.append("Parseo DTO → Entidad                     : ")
				.append(formatoTiempo(tiempoDTOParseoEntidades)).append("\n")
				.append("Parseo EESS                             : ")
				.append(formatoTiempo(tiempoParseoEESSMs)).append("\n")
				.append("Persistencia EESS y Precios (total)     : ")
				.append(formatoTiempo(tiempoPersistenciaMs)).append("\n")
				.append("============================================================\n");

		return sb.toString();
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
