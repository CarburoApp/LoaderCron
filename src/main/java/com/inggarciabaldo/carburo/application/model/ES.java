package com.inggarciabaldo.carburo.application.model;

import com.inggarciabaldo.carburo.application.model.enums.Margen;
import com.inggarciabaldo.carburo.application.model.enums.Remision;
import com.inggarciabaldo.carburo.application.model.enums.Venta;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
// TODO
public class ES implements Serializable {

	// ==============================
	// CAMPOS
	// ==============================

	private Integer id;
	private BigDecimal extCode;
	private String rotulo;
	private String horario;
	private String direccion;
	private String localidad;
	private BigDecimal codigoPostal;

	private Municipio municipio;
	private Provincia provincia;
	private BigDecimal latitud;
	private BigDecimal longitud;
	private Margen margen;
	private Remision remision;
	private Venta venta;
	private BigDecimal x100BioEtanol;
	private BigDecimal x100EsterMetilico;
	private final Set<Combustible> combustiblesDisponibles = new HashSet<>();


	/**
	 * Constructor principal para inicialización lógica.
	 */
	public ES(BigDecimal extCode, String rotulo, String horario, String direccion,
			  BigDecimal codigoPostal, String localidad, BigDecimal latitud,
			  BigDecimal longitud, Margen margen, Remision remision, Venta venta,
			  Municipio municipio, Provincia provincia) {
		setExtCode(extCode);
		setRotulo(rotulo);
		setHorario(horario);
		setDireccion(direccion);
		setLocalidad(localidad);
		setCodigoPostal(codigoPostal);
		setLatitud(latitud);
		setLongitud(longitud);
		setMargen(margen);
		setRemision(remision);
		setVenta(venta);
	}


	// ==============================
	// SETTERS CON VALIDACIÓN
	// ==============================

	public void setExtCode(BigDecimal extCode) {
		if (extCode == null || extCode.compareTo(BigDecimal.ZERO) < 0 ||
				extCode.stripTrailingZeros().scale() > 0) {
			throw new IllegalArgumentException(
					"extCode debe ser un número entero positivo o nulo.");
		}
		this.extCode = extCode;
	}

	public void setRotulo(String rotulo) {
		if (rotulo != null && rotulo.length() > 100) {
			throw new IllegalArgumentException(
					"El rótulo no puede superar los 100 caracteres.");
		}
		this.rotulo = rotulo;
	}

	public void setHorario(String horario) {
		if (horario != null && horario.length() > 100) {
			throw new IllegalArgumentException(
					"El horario no puede superar los 100 caracteres.");
		}
		this.horario = horario;
	}

	public void setDireccion(String direccion) {
		if (direccion != null && direccion.length() > 200) {
			throw new IllegalArgumentException(
					"La dirección no puede superar los 200 caracteres.");
		}
		this.direccion = direccion;
	}

	public void setLocalidad(String localidad) {
		if (localidad != null && localidad.length() > 100) {
			throw new IllegalArgumentException(
					"La localidad no puede superar los 100 caracteres.");
		}
		this.localidad = localidad;
	}

	public void setCodigoPostal(BigDecimal codigoPostal) {
		if (codigoPostal != null &&
				(codigoPostal.compareTo(BigDecimal.valueOf(1000)) < 0 ||
						 codigoPostal.compareTo(BigDecimal.valueOf(52999)) > 0)) {
			throw new IllegalArgumentException("Código postal no válido para España.");
		}
		this.codigoPostal = codigoPostal;
	}


	public void setLatitud(BigDecimal latitud) {
		if (latitud != null && (latitud.compareTo(BigDecimal.valueOf(-90)) < 0 ||
										latitud.compareTo(BigDecimal.valueOf(90)) > 0)) {
			throw new IllegalArgumentException("Latitud fuera de rango (-90 a 90).");
		}
		this.latitud = latitud;
	}

	public void setLongitud(BigDecimal longitud) {
		if (longitud != null && (longitud.compareTo(BigDecimal.valueOf(-180)) < 0 ||
										 longitud.compareTo(BigDecimal.valueOf(180)) >
												 0)) {
			throw new IllegalArgumentException("Longitud fuera de rango (-180 a 180).");
		}
		this.longitud = longitud;
	}

	public void setX100BioEtanol(BigDecimal x100BioEtanol) {
		if (x100BioEtanol != null && (x100BioEtanol.compareTo(BigDecimal.ZERO) < 0 ||
											  x100BioEtanol.compareTo(
													  BigDecimal.valueOf(100)) > 0)) {
			throw new IllegalArgumentException(
					"El porcentaje de bioetanol debe estar entre 0 y 100.");
		}
		this.x100BioEtanol = x100BioEtanol;
	}

	public void setX100EsterMetilico(BigDecimal x100EsterMetilico) {
		if (x100EsterMetilico != null &&
				(x100EsterMetilico.compareTo(BigDecimal.ZERO) < 0 ||
						 x100EsterMetilico.compareTo(BigDecimal.valueOf(100)) > 0)) {
			throw new IllegalArgumentException(
					"El porcentaje de éster metílico debe estar entre 0 y 100.");
		}
		this.x100EsterMetilico = x100EsterMetilico;
	}

	/**
	 * Asigna el margen de la estación.
	 *
	 * @param margen Margen, no puede ser null.
	 * @throws IllegalArgumentException si margen es null.
	 */
	public void setMargen(Margen margen) {
		if (margen == null)
			throw new IllegalArgumentException("El margen no puede ser null.");
		this.margen = margen;
	}

	/**
	 * Asigna la remisión de la estación.
	 *
	 * @param remision Remision, no puede ser null.
	 * @throws IllegalArgumentException si remision es null.
	 */
	public void setRemision(Remision remision) {
		if (remision == null)
			throw new IllegalArgumentException("La remisión no puede ser null.");
		this.remision = remision;
	}

	/**
	 * Asigna el tipo de venta de la estación.
	 *
	 * @param venta Venta, no puede ser null.
	 * @throws IllegalArgumentException si venta es null.
	 */
	public void setVenta(Venta venta) {
		if (venta == null)
			throw new IllegalArgumentException("La venta no puede ser null.");
		this.venta = venta;
	}

	// ==============================
	// MÉTODOS COMUNES
	// ==============================

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ES ES)) return false;
		return Objects.equals(id, ES.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "EESS{" + "id=" + id + ", rotulo='" + rotulo + '\'' + ", localidad='" +
				localidad + '\'' + ", codigoPostal=" + codigoPostal + ", provincia=" +
				(provincia != null ? provincia.getDenominacion() : null) + '}';
	}
}
