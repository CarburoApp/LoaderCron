package com.inggarciabaldo.carburo.application.model;

import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Getter
public class CA implements Serializable {

	// ==============================
	// CAMPOS
	// ==============================
	private short id;
	private String denominacion;
	private short extCode;

	/**
	 * Constructor lógico
	 */
	public CA(short id, String denominacion, short extCode) {
		setId(id);
		setDenominacion(denominacion);
		setExtCode(extCode);
	}

	// ==============================
	// SETTERS CON VALIDACIÓN
	// ==============================

	public void setId(short id) {
		if (id <= 0) throw new IllegalArgumentException(
				"El ID de la CCAA debe ser un número positivo.");
		this.id = id;
	}

	public void setDenominacion(String denominacion) {
		if (denominacion == null || denominacion.isBlank())
			throw new IllegalArgumentException(
					"La denominación de la CCAA no puede estar vacía.");
		if (denominacion.length() > 30) throw new IllegalArgumentException(
				"La denominación no puede superar los 30 caracteres.");
		this.denominacion = denominacion.trim();
	}

	public void setExtCode(short extCode) {
		if (extCode <= 0 || extCode > 99)  // verifica que esté entre 0 y 99
			throw new IllegalArgumentException(
					"El código externo debe estar entre 0 y 99.");

		this.extCode = extCode;
	}


	// ==============================
	// MÉTODOS COMUNES
	// ==============================


	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		CA ca = (CA) o;
		return id == ca.id && extCode == ca.extCode &&
				Objects.equals(denominacion, ca.denominacion);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, denominacion, extCode);
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("CA{");
		sb.append("id=").append(id);
		sb.append(", denominacion='").append(denominacion).append('\'');
		sb.append(", extCode=").append(extCode);
		sb.append('}');
		return sb.toString();
	}
}
