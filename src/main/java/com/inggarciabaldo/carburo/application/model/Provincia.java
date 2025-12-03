package com.inggarciabaldo.carburo.application.model;

import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Getter
public class Provincia implements Serializable {

	// ==============================
	// CAMPOS
	// ==============================
	private short id;
	private String denominacion;
	private short extCode;
	private CA CA;

	// ==============================
	// CONSTRUCTORES
	// ==============================

	/**
	 * Constructor lógico
	 */
	public Provincia(short id, String denominacion, short extCode, CA CA) {
		setId(id);
		setDenominacion(denominacion);
		setExtCode(extCode);
		setCA(CA);
	}

	// ==============================
	// SETTERS CON VALIDACIÓN
	// ==============================

	public void setId(short id) {
		if (id <= 0) throw new IllegalArgumentException(
				"El ID de la provincia debe ser un número positivo.");
		this.id = id;
	}

	public void setDenominacion(String denominacion) {
		if (denominacion == null || denominacion.isBlank())
			throw new IllegalArgumentException(
					"La denominación de la provincia no puede estar vacía.");
		if (denominacion.length() > 30) throw new IllegalArgumentException(
				"La denominación no puede superar los 30 caracteres.");
		this.denominacion = denominacion.trim();
	}

	public void setExtCode(short extCode) {
		if (extCode <= 0 || extCode > 99)
			throw new IllegalArgumentException(
					"El código externo debe ser un número entero positivo de hasta 2 dígitos (0-99): " +
							extCode);
		this.extCode = extCode;
	}


	public void setCA(CA CA) {
		if (CA == null) throw new IllegalArgumentException(
				"La provincia debe pertenecer a una CCAA.");
		this.CA = CA;
	}

	// ==============================
	// MÉTODOS COMUNES
	// ==============================


	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		Provincia provincia = (Provincia) o;
		return id == provincia.id && extCode == provincia.extCode &&
				Objects.equals(denominacion, provincia.denominacion) &&
				Objects.equals(CA, provincia.CA);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, denominacion, extCode, CA);
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("Provincia{");
		sb.append("id=").append(id);
		sb.append(", denominacion='").append(denominacion).append('\'');
		sb.append(", extCode=").append(extCode);
		sb.append(", CA=").append(CA);
		sb.append('}');
		return sb.toString();
	}
}
