package com.inggarciabaldo.carburo.application.model;

import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
// TODO
public class Combustible implements Serializable {

	private Short id;

	private String denominacion;

	private String codigo;

	private BigDecimal extCode;

	//Disponibilidad de combustible
	private final Set<ES> estaciones = new HashSet<>();


	// ======================
	// Constructores
	// ======================

	public Combustible(String denominacion, String codigo, BigDecimal extCode) {
		setDenominacion(denominacion);
		setCodigo(codigo);
		setExtCode(extCode);
	}

	// ======================
	// Setters con validación
	// ======================

	public void setId(Short id) {
		if (id != null && id < 0) throw new IllegalArgumentException(
				"El ID del combustible no puede ser negativo");
		this.id = id;
	}

	public void setDenominacion(String denominacion) {
		if (denominacion == null || denominacion.trim().isEmpty())
			throw new IllegalArgumentException(
					"La denominación del combustible es obligatoria");
		if (denominacion.length() > 50) throw new IllegalArgumentException(
				"La denominación no puede superar los 50 caracteres");
		this.denominacion = denominacion.trim();
	}

	public void setCodigo(String codigo) {
		if (codigo != null && codigo.length() > 10) throw new IllegalArgumentException(
				"El código del combustible no puede superar los 10 caracteres");
		this.codigo = (codigo != null) ? codigo.trim() : null;
	}

	public void setExtCode(BigDecimal extCode) {
		if (extCode == null)
			throw new IllegalArgumentException("El código externo no puede ser nulo.");
		if (extCode.scale() != 0) // verifica que sea un entero
			throw new IllegalArgumentException(
					"El código externo debe ser un número entero.");
		if (extCode.compareTo(BigDecimal.ZERO) < 0 ||
				extCode.compareTo(BigDecimal.valueOf(99)) > 0)
			throw new IllegalArgumentException(
					"El código externo debe estar entre 0 y 99.");

		this.extCode = extCode;
	}

	// ======================
	// equals & hashCode
	// ======================

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Combustible that)) return false;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	// ======================
	// toString
	// ======================

	@Override
	public String toString() {
		return "Combustible{" + "id=" + id + ", denominacion='" + denominacion + '\'' +
				", codigo='" + codigo + '\'' + ", extCode=" + extCode + '}';
	}
}
