package com.inggarciabaldo.carburo.model.entity.geo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Provincia")
@Getter
@NoArgsConstructor
public class Provincia implements Serializable {

    // ==============================
    // CAMPOS
    // ==============================

    @Id
    private Short id;

    @Column(length = 30, nullable = false)
    private String denominacion;

    @Column(name = "ext_code", precision = 2, scale = 0)
    private BigDecimal extCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_CCAA", nullable = false)
    private CA CA;

    @OneToMany(mappedBy = "provincia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<com.inggarciabaldo.carburo.model.entity.geo.Municipio> municipios;

    // ==============================
    // CONSTRUCTORES
    // ==============================

    /**
     * Constructor lógico
     */
    public Provincia(short id, String denominacion, BigDecimal extCode, CA CA) {
        setId(id);
        setDenominacion(denominacion);
        setExtCode(extCode);
        setCA(CA);
    }

    // ==============================
    // SETTERS CON VALIDACIÓN
    // ==============================

    public void setId(short id) {
        if (id <= 0)
            throw new IllegalArgumentException("El ID de la provincia debe ser un número positivo.");
        this.id = id;
    }

    public void setDenominacion(String denominacion) {
        if (denominacion == null || denominacion.isBlank())
            throw new IllegalArgumentException("La denominación de la provincia no puede estar vacía.");
        if (denominacion.length() > 30)
            throw new IllegalArgumentException("La denominación no puede superar los 30 caracteres.");
        this.denominacion = denominacion.trim();
    }

    public void setExtCode(BigDecimal extCode) {
        if (extCode == null)
            throw new IllegalArgumentException("El código externo no puede ser nulo.");

        if (extCode.compareTo(BigDecimal.ZERO) < 0 || extCode.compareTo(new BigDecimal("99")) > 0)
            throw new IllegalArgumentException("El código externo debe ser un número entero positivo de hasta 2 dígitos: " + extCode);

        this.extCode = extCode;
    }


    public void setCA(CA CA) {
        if (CA == null)
            throw new IllegalArgumentException("La provincia debe pertenecer a una CCAA.");
        this.CA = CA;
    }

    // ==============================
    // MÉTODOS COMUNES
    // ==============================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Provincia that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Provincia{" +
                "id=" + id +
                ", denominacion='" + denominacion + '\'' +
                ", extCode=" + extCode +
                ", ccaa=" + (CA != null ? CA.getDenominacion() : null) +
                '}';
    }
}
