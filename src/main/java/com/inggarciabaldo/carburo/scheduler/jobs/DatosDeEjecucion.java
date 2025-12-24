package com.inggarciabaldo.carburo.scheduler.jobs;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase para gestionar los datos referentes a estadisticas de ejecución del cron.
 */
@Getter
@Setter
public class DatosDeEjecucion {

	// Fechas de ejecución
	// Guarda la fecha y hora exacta de inicio del cron
	private LocalDateTime fechaInicioEjecucion = LocalDateTime.now();
	// Guarda la fecha y hora exacta de finalización del cron
	private LocalDateTime fechaFinEjecucion = LocalDateTime.now();
	// Guarda la fecha lógica de los datos procesados (fecha de parser/API)
	private LocalDate fechaDeDatosProcesados = LocalDate.now();

	// Archivos
	// Guarda el archivo JSON bruto recibido desde la API
	private File archivoRespuestaApiJson;

	// Tiempos globales - de alto nivel
	// Guarda el tiempo total de ejecución del cron completo
	private long tiempoTotalCronMs;
	// Guarda el tiempo total invertido en fase de petición HTTP
	private long tiempoTotalHttpRequestMs;
	// Guarda el tiempo total invertido en fase de parseo
	private long tiempoTotalParseoMs;
	// Guarda el tiempo total invertido en fase de persistencia
	private long tiempoTotalPersistenciaMs;

	// PETICIÓN A API EXTERNA
	// Guarda el número total de intentos realizados contra la API
	private int apiHttpIntentosRealizados;
	// Guarda el número total de estaciones recibidas en el JSON
	private int apiTotalEESSRecibidasJson;
	// Guarda si la respuesta de la API fue estructuralmente válida
	private boolean apiRespuestaValida;

	//Paseo JSON -> DTO
	// Guarda el tiempo empleado en transformar JSON a DTOs
	private long parseoJsonADtoTiempoMs;
	// Guarda el número total de estaciones convertidas a DTO
	private int parseoTotalEESSEnDto;

	//Paseo DTO -> Entidades de dominio
	// Guarda el número total de estaciones que se han parseado
	private int parseoEESSTotal;
	// Guarda el número total de estaciones parseadas correctamente
	private int parseoEESSCorrectas;
	// Guarda el número de estaciones descartadas por error
	private int parseoEESSErroneas;
	// Guarda el número total de precios parseados correctamente
	private int parseoPreciosCorrectos;

	// Carga de datos iniciales desde la BD
	// Guarda el tiempo empleado en cargar todas las estaciones existentes desde la BD
	private long bdCargaInicialEESSTiempoMs;
	// Guarda el número total de estaciones existentes en BD al inicio
	private int bdTotalEESSIniciales;

	// ANÁLISIS DE DIFERENCIAS
	// Guarda el tiempo empleado en determinar qué estaciones requieren actualización
	private long decisionEESSActualizarTiempoMs;
	// Guarda el tiempo empleado en determinar qué disponibilidades requieren inserción
	private long decisionDisponibilidadInsertarTiempoMs;
	// Guarda el tiempo empleado en determinar qué precios requieren inserción
	private long decisionPreciosInsertarTiempoMs;
	// Guarda el tiempo empleado en determinar qué precios requieren actualización
	private long decisionPreciosActualizarTiempoMs;
	// Guarda el número de estaciones nuevas detectadas
	private int decisionEESSNuevas;
	// Guarda el número de estaciones detectadas que ya se encontraban registradas
	private int decisionEESSYaPresentes;
	// Guarda el número de estaciones existentes que requieren actualización, subconjunto del las YA PRESENTES
	private int decisionEESSActualizar;
	// Guarda el número de precios nuevos detectados, subconjunto del las YA PRESENTES
	private int decisionPreciosInsertar;
	// Guarda el número de precios que requieren actualización, subconjunto del las YA PRESENTES
	private int decisionPreciosActualizar;
	// Guarda el número de disponibilidades nuevas detectadas, subconjunto del las YA PRESENTES
	private int decisionDisponibilidadesInsertar;

	// PERSISTENCIA EN BD - EESS
	// Guarda el tiempo de inserción de nuevas estaciones, junto a sus precios y disponibilidad
	private long persistenciaEESSInsertTiempoMs;
	// Guarda el número de estaciones insertadas en BD
	private int persistenciaEESSInsertadas;
	// Guarda el tiempo de actualización de estaciones existentes
	private long persistenciaEESSUpdateTiempoMs;
	// Guarda el número de estaciones actualizadas en BD
	private int persistenciaEESSActualizadas;


	// PERSISTENCIA EN BD - Precio - Combustible
	// Guarda el tiempo de inserción de precios nuevos
	private long persistenciaPreciosInsertTiempoMs;
	// Guarda el número de precios insertados
	private int persistenciaPreciosInsertados;
	// Guarda el tiempo de actualización de precios existentes
	private long persistenciaPreciosUpdateTiempoMs;
	// Guarda el número de precios actualizados
	private int persistenciaPreciosActualizados;

	// PERSISTENCIA EN BD - Disponibilidad - Combustible
	// Guarda el tiempo de inserción de disponibilidades
	private long persistenciaDisponibilidadesInsertTiempoMs;
	// Guarda el número de disponibilidades insertadas
	private int persistenciaDisponibilidadesInsertadas;


	// Guarda la lista completa de errores detectados durante la ejecución
	private final List<String> erroresDetectados = new ArrayList<>();

	// Guarda la lista completa de avisos no críticos detectados
	private final List<String> warningsDetectados = new ArrayList<>();

	protected DatosDeEjecucion() {}

	/*
	 * Métodos auxiliares
	 */

	public void addWarning(String warning) {
		warningsDetectados.add(warning);
	}

	public String getInformeEjecucion() {
		return "\n============================================================\n" +
				"                     RESUMEN EJECUCIÓN CRON\n" +
				"============================================================\n" +

				"FECHAS\n------------------------------------------------------------\n" +
				"Inicio ejecución             : " + formatoHora(fechaInicioEjecucion) +
				"\n" + "Fin ejecución                : " +
				formatoHora(fechaFinEjecucion) + "\n" +
				"Fecha de datos procesados    : " + fechaDeDatosProcesados + "\n" +
				"Duración total               : " + formatoTiempo(tiempoTotalCronMs) +
				"\n\n" +

				"PETICIÓN A API EXTERNA\n------------------------------------------------------------\n" +
				"Intentos realizados          : " + apiHttpIntentosRealizados + "\n" +
				"Total EESS recibidas (JSON)  : " + apiTotalEESSRecibidasJson + "\n" +
				"Respuesta API válida          : " + apiRespuestaValida + "\n" +
				"Tiempo HTTP                  : " +
				formatoTiempo(tiempoTotalHttpRequestMs) + "\n\n" +

				"PARSEO JSON → DTO\n------------------------------------------------------------\n" +
				"Tiempo parseo JSON → DTO      : " +
				formatoTiempo(parseoJsonADtoTiempoMs) + "\n" +
				"Total EESS en DTO             : " + parseoTotalEESSEnDto + "\n\n" +

				"PARSEO DTO → ENTIDADES\n------------------------------------------------------------\n" +
				"Total EESS parseadas          : " + parseoEESSTotal + "\n" +
				"EESS correctas                : " + parseoEESSCorrectas + "\n" +
				"EESS erróneas                 : " + parseoEESSErroneas + "\n" +
				"Precios correctos             : " + parseoPreciosCorrectos + "\n" +
				"Tiempo parseo total           : " + formatoTiempo(tiempoTotalParseoMs) +
				"\n\n" +

				"CARGA INICIAL BD\n------------------------------------------------------------\n" +
				"Tiempo carga EESS BD inicial  : " +
				formatoTiempo(bdCargaInicialEESSTiempoMs) + "\n" +
				"Total EESS en BD al inicio    : " + bdTotalEESSIniciales + "\n\n" +

				"DECISIONES / ANÁLISIS DIFERENCIAS\n------------------------------------------------------------\n" +
				"Tiempo decisión actualización EESS          : " +
				formatoTiempo(decisionEESSActualizarTiempoMs) + "\n" +
				"Tiempo decisión insertar disponibilidades  : " +
				formatoTiempo(decisionDisponibilidadInsertarTiempoMs) + "\n" +
				"Tiempo decisión insertar precios          : " +
				formatoTiempo(decisionPreciosInsertarTiempoMs) + "\n" +
				"Tiempo decisión actualizar precios        : " +
				formatoTiempo(decisionPreciosActualizarTiempoMs) + "\n" +
				"EESS nuevas                               : " + decisionEESSNuevas +
				"\n" + "EESS ya presentes                          : " +
				decisionEESSYaPresentes + "\n" +
				"EESS a actualizar                          : " + decisionEESSActualizar +
				"\n" + "Precios a insertar                          : " +
				decisionPreciosInsertar + "\n" +
				"Precios a actualizar                        : " +
				decisionPreciosActualizar + "\n" +
				"Disponibilidades a insertar                 : " +
				decisionDisponibilidadesInsertar + "\n\n" +

				"PERSISTENCIA EESS\n------------------------------------------------------------\n" +
				"Tiempo inserción EESS                       : " +
				formatoTiempo(persistenciaEESSInsertTiempoMs) + "\n" +
				"EESS insertadas                             : " +
				persistenciaEESSInsertadas + "\n" +
				"Tiempo actualización EESS                   : " +
				formatoTiempo(persistenciaEESSUpdateTiempoMs) + "\n" +
				"EESS actualizadas                           : " +
				persistenciaEESSActualizadas + "\n\n" +

				"PERSISTENCIA PRECIOS\n------------------------------------------------------------\n" +
				"Tiempo inserción precios                     : " +
				formatoTiempo(persistenciaPreciosInsertTiempoMs) + "\n" +
				"Precios insertados                           : " +
				persistenciaPreciosInsertados + "\n" +
				"Tiempo actualización precios                 : " +
				formatoTiempo(persistenciaPreciosUpdateTiempoMs) + "\n" +
				"Precios actualizados                         : " +
				persistenciaPreciosActualizados + "\n\n" +

				"PERSISTENCIA DISPONIBILIDADES\n------------------------------------------------------------\n" +
				"Tiempo inserción disponibilidades           : " +
				formatoTiempo(persistenciaDisponibilidadesInsertTiempoMs) + "\n" +
				"Disponibilidades insertadas                 : " +
				persistenciaDisponibilidadesInsertadas + "\n\n" +

				"ERRORES Y WARNINGS\n------------------------------------------------------------\n" +
				"Errores detectados                           : " +
				erroresDetectados.size() + "\n" +
				"Warnings detectados                          : " +
				warningsDetectados.size() + "\n" +
				"============================================================\n";
	}



	/**
	 * Formatea milisegundos a minutos y segundos legibles
	 */
	public String formatoTiempo(long millis) {
		Duration duration = Duration.ofMillis(millis);
		long minutos = duration.toMinutes();
		long segundos = duration.minusMinutes(minutos).getSeconds();
		long milisegundos = millis % 1000;
		return String.format("%dm %ds %dms", minutos, segundos, milisegundos);
	}

	/**
	 * Formatea hora legible
	 */
	public String formatoHora(LocalDateTime fecha) {
		return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
	}

	public String formatoDiaHoraMinuto(LocalDateTime fecha) {
		return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
	}


}
