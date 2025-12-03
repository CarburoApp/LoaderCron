package com.inggarciabaldo.carburo.model.entity.eess;

import com.inggarciabaldo.carburo.model.entity.Relaciones;
import com.inggarciabaldo.carburo.model.entity.geo.Municipio;
import com.inggarciabaldo.carburo.model.entity.geo.Provincia;
import com.inggarciabaldo.carburo.model.enums.Margen;
import com.inggarciabaldo.carburo.model.enums.Remision;
import com.inggarciabaldo.carburo.model.enums.Venta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
public class ES implements Serializable {

    // ==============================
    // CAMPOS
    // ==============================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ext_code")
    private BigDecimal extCode;

    @Column(length = 100)
    private String rotulo;

    @Column(length = 50)
    private String horario;

    @Column(length = 200)
    private String direccion;

    @Column(length = 50)
    private String localidad;

    @Column(name = "codigo_postal", precision = 5)
    private BigDecimal codigoPostal;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_municipio")
    @Setter
    private Municipio municipio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_provincia")
    @Setter
    private Provincia provincia;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitud;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitud;

    @Enumerated(EnumType.STRING)
    private Margen margen;

    @Enumerated(EnumType.STRING)
    private Remision remision;

    @Enumerated(EnumType.STRING)
    private Venta venta;

    @Column(name = "x100_bio_etanol", precision = 6, scale = 3)
    private BigDecimal x100BioEtanol;

    @Column(name = "x100_ester_metilico", precision = 6, scale = 3)
    private BigDecimal x100EsterMetilico;

    @ManyToMany
    @JoinTable(
            name = "combustibledisponible",
            joinColumns = @JoinColumn(name = "id_eess"),
            inverseJoinColumns = @JoinColumn(name = "id_combustible")
    )
    private Set<Combustible> combustiblesDisponibles = new HashSet<>();


    /**
     * Constructor principal para inicialización lógica.
     */
    public ES(
            BigDecimal extCode,
            String rotulo,
            String horario,
            String direccion,
            BigDecimal codigoPostal,
            String localidad,
            BigDecimal latitud,
            BigDecimal longitud,
            Margen margen,
            Remision remision,
            Venta venta,
            Municipio municipio,
            Provincia provincia
    ) {
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
        Relaciones.EESSLocalizacion.vincular(this, municipio, provincia);
    }


    // ==============================
    // SETTERS CON VALIDACIÓN
    // ==============================

    public void setExtCode(BigDecimal extCode) {
        if (extCode == null || extCode.compareTo(BigDecimal.ZERO) < 0 || extCode.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("extCode debe ser un número entero positivo o nulo.");
        }
        this.extCode = extCode;
    }

    public void setRotulo(String rotulo) {
        if (rotulo != null && rotulo.length() > 100) {
            throw new IllegalArgumentException("El rótulo no puede superar los 100 caracteres.");
        }
        this.rotulo = rotulo;
    }

    public void setHorario(String horario) {
        if (horario != null && horario.length() > 100) {
            throw new IllegalArgumentException("El horario no puede superar los 100 caracteres.");
        }
        this.horario = horario;
    }

    public void setDireccion(String direccion) {
        if (direccion != null && direccion.length() > 200) {
            throw new IllegalArgumentException("La dirección no puede superar los 200 caracteres.");
        }
        this.direccion = direccion;
    }

    public void setLocalidad(String localidad) {
        if (localidad != null && localidad.length() > 100) {
            throw new IllegalArgumentException("La localidad no puede superar los 100 caracteres.");
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
                longitud.compareTo(BigDecimal.valueOf(180)) > 0)) {
            throw new IllegalArgumentException("Longitud fuera de rango (-180 a 180).");
        }
        this.longitud = longitud;
    }

    public void setX100BioEtanol(BigDecimal x100BioEtanol) {
        if (x100BioEtanol != null && (x100BioEtanol.compareTo(BigDecimal.ZERO) < 0 ||
                x100BioEtanol.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new IllegalArgumentException("El porcentaje de bioetanol debe estar entre 0 y 100.");
        }
        this.x100BioEtanol = x100BioEtanol;
    }

    public void setX100EsterMetilico(BigDecimal x100EsterMetilico) {
        if (x100EsterMetilico != null && (x100EsterMetilico.compareTo(BigDecimal.ZERO) < 0 ||
                x100EsterMetilico.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new IllegalArgumentException("El porcentaje de éster metílico debe estar entre 0 y 100.");
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
        return "EESS{" +
                "id=" + id +
                ", rotulo='" + rotulo + '\'' +
                ", localidad='" + localidad + '\'' +
                ", codigoPostal=" + codigoPostal +
                ", provincia=" + (provincia != null ? provincia.getDenominacion() : null) +
                '}';
    }
}
