package com.inggarciabaldo.carburo.application.model;

import com.inggarciabaldo.carburo.application.model.enums.Margen;
import com.inggarciabaldo.carburo.application.model.enums.Remision;
import com.inggarciabaldo.carburo.application.model.enums.Venta;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
public class EstacionDeServicio implements Serializable {

	// ==============================
	// CAMPOS
	// ==============================
	private int id;
	private int extCode;
	private String rotulo;
	private String horario;
	private String direccion;
	private String localidad;
	private int codigoPostal;

	private Municipio municipio;
	private Provincia provincia;
	private double latitud;
	private double longitud;
	private Margen margen;
	private Remision remision;
	private Venta venta;
	private double x100BioEtanol;
	private double x100EsterMetilico;

	// Otros datos
	private final Set<Combustible> combustiblesDisponibles = new HashSet<>();
	private final Set<PrecioCombustible> preciosCombustibles = new HashSet<>();

	public EstacionDeServicio(int id, int extCode, String rotulo, String horario,
							  String direccion, String localidad, int codigoPostal,
							  Municipio municipio, Provincia provincia, double latitud,
							  double longitud, Margen margen, Remision remision,
							  Venta venta, double x100BioEtanol,
							  double x100EsterMetilico) {
		setId(id);
		setExtCode(extCode);
		setRotulo(rotulo);
		setHorario(horario);
		setDireccion(direccion);
		setProvincia(provincia);
		setMunicipio(municipio);
		setLocalidad(localidad);
		setCodigoPostal(codigoPostal);
		setLatitud(latitud);
		setLongitud(longitud);
		setMargen(margen);
		setRemision(remision);
		setVenta(venta);

		setX100BioEtanol(x100BioEtanol);
		setX100EsterMetilico(x100EsterMetilico);
	}

	public EstacionDeServicio(int id, int extCode, String rotulo, String horario,
							  String direccion, String localidad, int codigoPostal,
							  Municipio municipio, Provincia provincia, double latitud,
							  double longitud, Margen margen, Remision remision,
							  Venta venta, double x100BioEtanol, double x100EsterMetilico,
							  List<Combustible> combustiblesDisponibles) {
		this(id, extCode, rotulo, horario, direccion, localidad, codigoPostal, municipio,
			 provincia, latitud, longitud, margen, remision, venta, x100BioEtanol,
			 x100EsterMetilico);
		setCombustiblesDisponibles(combustiblesDisponibles);
	}


	public boolean addPrecioCombustible(double precio, Combustible combustible,
										LocalDate fecha) {
		PrecioCombustible precioC = new PrecioCombustible(this, combustible, fecha,
														  precio);
		return this.preciosCombustibles.add(precioC);
	}

	public boolean addCombustibleDisponible(Combustible combustible) {
		return this.combustiblesDisponibles.add(combustible);
	}

	public boolean removeCombustibleDisponible(Combustible combustible) {
		return this.combustiblesDisponibles.remove(combustible);
	}


	// ==============================
	// SETTERS CON VALIDACIÓN
	// ==============================

	public void setId(int id) {
		if (id < 0) throw new IllegalArgumentException(
				"El id debe ser un número entero positivo: " + id);
		this.id = id;
	}

	public void setExtCode(int extCode) {
		if (extCode < 0) {
			throw new IllegalArgumentException(
					"El extCode o código externo debe ser un número entero positivo: " +
							extCode);
		}
		this.extCode = extCode;
	}

	public void setRotulo(String rotulo) {
		if (rotulo != null && !rotulo.isBlank() && rotulo.length() > 100) {
			throw new IllegalArgumentException(
					"El rótulo no puede ser nulo, blanco o superar los 100 caracteres: " +
							rotulo);
		}
		this.rotulo = rotulo;
	}

	public void setHorario(String horario) {
		if (horario != null && !horario.isBlank() && horario.length() > 100) {
			throw new IllegalArgumentException(
					"El horario no puede ser nulo, blanco o superar los 100 caracteres: " +
							horario);
		}
		this.horario = horario;
	}

	public void setDireccion(String direccion) {
		if (direccion != null && !direccion.isBlank() && direccion.length() > 200) {
			throw new IllegalArgumentException(
					"La dirección no puede ser nulo, blanco o superar los 200 caracteres: " +
							direccion);
		}
		this.direccion = direccion;
	}

	public void setLocalidad(String localidad) {
		if (localidad != null && !localidad.isBlank() && localidad.length() > 100) {
			throw new IllegalArgumentException(
					"La localidad no puede ser nulo, blanco o superar los 100 caracteres: " +
							localidad);
		}
		this.localidad = localidad;
	}

	public void setMunicipio(Municipio municipio) {
		if (municipio == null)
			throw new IllegalArgumentException("El Municipio no puede ser nulo.");
		this.municipio = municipio;
	}

	public void setProvincia(Provincia provincia) {
		if (provincia == null)
			throw new IllegalArgumentException("La provincia no puede ser nula.");
		this.provincia = provincia;

	}

	public void setCodigoPostal(int codigoPostal) {
		if (codigoPostal < 1 || codigoPostal > 52999) {
			throw new IllegalArgumentException(
					"Código postal no válido para España: " + codigoPostal);
		}
		this.codigoPostal = codigoPostal;
	}


	public void setLatitud(double latitud) {
		if (latitud > 180 || latitud < -180) throw new IllegalArgumentException(
				"Latitud fuera de rango (-90 a 90): " + latitud);
		this.latitud = latitud;
	}

	public void setLongitud(double longitud) {
		if (longitud > 180 || longitud < -180) throw new IllegalArgumentException(
				"Longitud fuera de rango (-180 a 180): " + longitud);
		this.longitud = longitud;
	}

	public void setX100BioEtanol(double x100BioEtanol) {
		if (x100BioEtanol < 0 || x100BioEtanol > 100)
			throw new IllegalArgumentException(
					"El porcentaje de bioetanol debe estar entre 0 y 100: " +
							x100BioEtanol);
		this.x100BioEtanol = x100BioEtanol;
	}

	public void setX100EsterMetilico(double x100EsterMetilico) {
		if (x100EsterMetilico < 0 || x100EsterMetilico > 100)
			throw new IllegalArgumentException(
					"El porcentaje de éster metílico debe estar entre 0 y 100: " +
							x100EsterMetilico);
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
			throw new IllegalArgumentException("El margen no puede ser nulo.");
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
			throw new IllegalArgumentException("La remisión no puede ser nulo.");
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
			throw new IllegalArgumentException("La venta no puede ser nulo.");
		this.venta = venta;
	}

	private void setCombustiblesDisponibles(List<Combustible> combustiblesDisponibles) {
		if (combustiblesDisponibles == null) throw new IllegalArgumentException(
				"La lista de combustibles disponibles no puede ser nula.");
		this.combustiblesDisponibles.addAll(combustiblesDisponibles);
	}

	// ==============================
	// MÉTODOS COMUNES
	// ==============================

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof EstacionDeServicio estacionDeServicio)) return false;
		return Objects.equals(id, estacionDeServicio.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "ES{" + "id=" + id + ", extCode=" + extCode + ", rotulo='" + rotulo +
				'\'' + ", horario='" + horario + '\'' + ", direccion='" + direccion +
				'\'' + ", localidad='" + localidad + '\'' + ", codigoPostal=" +
				codigoPostal + ", municipio=" + municipio.getDenominacion() +
				", provincia=" + provincia.getDenominacion() + ", latitud=" + latitud +
				", longitud=" + longitud + ", margen=" + margen + ", remision=" +
				remision + ", venta=" + venta + ", x100BioEtanol=" + x100BioEtanol +
				", x100EsterMetilico=" + x100EsterMetilico + '}';
	}
}
