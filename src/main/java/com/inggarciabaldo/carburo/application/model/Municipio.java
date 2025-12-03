package com.inggarciabaldo.carburo.model.entity.geo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "Municipio")
@Getter
@NoArgsConstructor
public class Municipio implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false, length = 60)
    private String denominacion;

    @Column(name = "ext_code", precision = 4, scale = 0)
    private BigDecimal extCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_provincia", nullable = false)
    private Provincia provincia;

    // ======================
    // Constructores
    // ======================

    public Municipio(String denominacion, BigDecimal extCode, Provincia provincia) {
        setDenominacion(denominacion);
        setExtCode(extCode);
        setProvincia(provincia);
    }

    // ======================
    // Setters con validación
    // ======================

    public void setId(Short id) {
        if (id != null && id < 0)
            throw new IllegalArgumentException("El ID de municipio no puede ser negativo");
        this.id = id;
    }

    public void setDenominacion(String denominacion) {
        if (denominacion == null || denominacion.trim().isEmpty())
            throw new IllegalArgumentException("La denominación del municipio es obligatoria");
        if (denominacion.length() > 60)
            throw new IllegalArgumentException("La denominación no puede superar los 50 caracteres");
        this.denominacion = denominacion.trim();
    }

    public void setExtCode(BigDecimal extCode) {
        if (extCode != null && (extCode.compareTo(BigDecimal.ZERO) < 0
                || extCode.compareTo(BigDecimal.valueOf(9999)) > 0)) {
            throw new IllegalArgumentException("El código externo del municipio debe tener entre 0 y 9999");
        }
        this.extCode = extCode;
    }


    public void setProvincia(Provincia provincia) {
        if (provincia == null)
            throw new IllegalArgumentException("El municipio debe estar asociado a una provincia");
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
        return "Municipio{" +
                "id=" + id +
                ", denominacion='" + denominacion + '\'' +
                ", extCode=" + extCode +
                ", provincia=" + (provincia != null ? provincia.getId() : "null") +
                '}';
    }
}
