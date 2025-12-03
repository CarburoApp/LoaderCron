package com.inggarciabaldo.carburo.application.model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad que representa el precio de un combustible en una EESS (estación de servicio)
 * en una fecha determinada.
 */
public class PrecioCombustible implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	// ==============================
	// CONSTANTES
	// ==============================
	public static final LocalDate FECHA_MIN = LocalDate.of(2003, 1, 1);
	public static final LocalDate FECHA_MAX = LocalDate.now();
	public static final double PRECIO_MAX = 20.000; // límite máximo del precio

	// ==============================
	// ATRIBUTOS
	// ==============================
	private LocalDate fecha;
	private Combustible combustible;
	private ES es;
	private double precio;

	// ==============================
	// CONSTRUCTOR
	// ==============================
	public PrecioCombustible(ES es, Combustible combustible, LocalDate fecha,
							 double precio) {
		setEs(es);
		setCombustible(combustible);
		setFecha(fecha);
		setPrecio(precio);
	}

	// ==============================
	// SETTERS CON VALIDACIÓN
	// ==============================
	public void setEs(ES es) {
		if (es == null) {
			throw new IllegalArgumentException(
					"La estación de servicio (ES) no puede ser nula");
		}
		this.es = es;
	}

	public void setCombustible(Combustible combustible) {
		if (combustible == null) {
			throw new IllegalArgumentException("El combustible no puede ser nulo");
		}
		this.combustible = combustible;
	}

	public void setFecha(LocalDate fecha) {
		if (fecha == null) {
			throw new IllegalArgumentException("La fecha no puede ser nula");
		}
		if (fecha.isBefore(FECHA_MIN) || fecha.isAfter(FECHA_MAX)) {
			throw new IllegalArgumentException(
					"La fecha debe estar entre " + FECHA_MIN + " y " + FECHA_MAX);
		}
		this.fecha = fecha;
	}

	public void setPrecio(double precio) {
		if (precio <= 0 || precio > PRECIO_MAX) throw new IllegalArgumentException(
				"El precio debe ser mayor que 0 y menos que: " + PRECIO_MAX +
						" . Actualmente: " + precio);
		this.precio = BigDecimal.valueOf(precio).setScale(3, RoundingMode.HALF_UP)
				.doubleValue();
	}

	// ==============================
	// GETTERS
	// ==============================
	public LocalDate getFecha() {
		return fecha;
	}

	public Combustible getCombustible() {
		return combustible;
	}

	public ES getEs() {
		return es;
	}

	public double getPrecio() {
		return precio;
	}

	// ==============================
	// MÉTODOS COMUNES
	// ==============================
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PrecioCombustible that)) return false;
		return Objects.equals(fecha, that.fecha) &&
				Objects.equals(combustible, that.combustible) &&
				Objects.equals(es, that.es) && Objects.equals(precio, that.precio);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fecha, combustible, es, precio);
	}

	@Override
	public String toString() {
		return "PrecioCombustible{" + "fecha=" + fecha.toString() + ", combustible=" +
				combustible.getDenominacion() + ", ES=" + es.getId() + ", precio=" +
				precio + '}';
	}
}
