package com.inggarciabaldo.carburo.application.model;

import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Getter
public class Municipio implements Serializable {

	private short id;
	private String denominacion;
	private short extCode;
	private Provincia provincia;

	// ======================
	// Constructores
	// ======================

	public Municipio(short id, String denominacion, short extCode, Provincia provincia) {
		setID(id);
		setDenominacion(denominacion);
		setExtCode(extCode);
		setProvincia(provincia);
	}

	// ======================
	// Setters con validación
	// ======================

	public void setID(short id) {
		if (id < 0) throw new IllegalArgumentException(
				"El ID de municipio no puede ser negativo");
		this.id = id;
	}

	public void setDenominacion(String denominacion) {
		if (denominacion == null || denominacion.trim().isEmpty())
			throw new IllegalArgumentException(
					"La denominación del municipio es obligatoria");
		if (denominacion.length() > 60) throw new IllegalArgumentException(
				"La denominación no puede superar los 50 caracteres");
		this.denominacion = denominacion.trim();
	}

	public void setExtCode(short extCode) {
		if (extCode < 0 || extCode > 9999) {
			throw new IllegalArgumentException(
					"El código externo del municipio debe tener entre 0 y 9999");
		}
		this.extCode = extCode;
	}

	public void setProvincia(Provincia provincia) {
		if (provincia == null) throw new IllegalArgumentException(
				"El municipio debe estar asociado a una provincia");
		this.provincia = provincia;
	}

	// ======================
	// equals & hashCode
	// ======================

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Municipio that)) return false;
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
		final StringBuilder sb = new StringBuilder("Municipio{");
		sb.append("id=").append(id);
		sb.append(", denominacion='").append(denominacion).append('\'');
		sb.append(", extCode=").append(extCode);
		sb.append(", provincia=").append(provincia.getDenominacion());
		sb.append('}');
		return sb.toString();
	}
}
