package com.inggarciabaldo.carburo.application.model;

import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Getter
public class Combustible implements Serializable {

	/**
	 * Atributos de la clase
	 */
	private short id;
	private String denominacion;
	private String codigo;
	private short extCode;

	// ======================
	// Constructores
	// ======================

	/**
	 * Constructor completo de la clase Combustible
	 *
	 * @param id           del combustible
	 * @param denominacion del combustible
	 * @param codigo       del combustible
	 * @param extCode      código externo (API) del combustible
	 */
	public Combustible(short id, String denominacion, String codigo, short extCode) {
		setId(id);
		setDenominacion(denominacion);
		setCodigo(codigo);
		setExtCode(extCode);
	}

	// ======================
	// Setters con validación
	// ======================

	public void setId(short id) {
		if (id < 0) throw new IllegalArgumentException(
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

	public void setExtCode(short extCode) {
		if (extCode < 0) // verifica que sea un entero
			throw new IllegalArgumentException(
					"El código externo debe ser un número entero.");
		if (extCode > 99)
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
