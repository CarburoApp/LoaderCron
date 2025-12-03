package com.inggarciabaldo.carburo.application.model;

import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Entidad que representa el precio de un combustible en una EESS (estación de servicio)
 * en una fecha determinada.
 */
@Getter
// TODO
public class PrecioCombustible implements Serializable {

	private final LocalDate fecha;
	private final Combustible combustible;
	private final ES ES;

	private BigDecimal precio;


	public PrecioCombustible(ES ES, Combustible combustible, LocalDate fecha,
							 LocalDate fecha1,
							 BigDecimal precio) {
		this.fecha = fecha1;
		if (ES == null || combustible == null)
			throw new IllegalArgumentException("EESS y Combustible no pueden ser nulos");
		if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0)
			throw new IllegalArgumentException("El precio debe ser mayor que 0");
		if (fecha == null || fecha.isAfter(LocalDate.now()))
			throw new IllegalArgumentException("Fecha inválida");

		this.ES          = ES;
		this.combustible = combustible;
		this.precio      = precio.setScale(3, RoundingMode.HALF_UP);
	}

	// ==============================
	// SETTERS CON VALIDACIÓN
	// ==============================
	public void setPrecio(BigDecimal precio) {
		if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0)
			throw new IllegalArgumentException("El precio debe ser mayor que 0");
		this.precio = precio.setScale(3, RoundingMode.HALF_UP);
	}

	// ==============================
	// MÉTODOS COMUNES
	// ==============================
}
