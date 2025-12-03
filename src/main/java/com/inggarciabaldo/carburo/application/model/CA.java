package com.inggarciabaldo.carburo.model.entity.geo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "CCAA")
@Getter
@NoArgsConstructor
public class CA implements Serializable {

    // ==============================
    // CAMPOS
    // ==============================

    @Id
    private Short id;

    @Column(length = 30, nullable = false)
    private String denominacion;

    @Column(name = "ext_code", precision = 2)
    private BigDecimal extCode;

    @OneToMany(mappedBy = "ccaa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Provincia> provincias;

    // ==============================
    // CONSTRUCTORES
    // ==============================

    /**
     * Constructor lógico
     */
    public CA(Short id, String denominacion, BigDecimal extCode) {
        setId(id);
        setDenominacion(denominacion);
        setExtCode(extCode);
    }

    // ==============================
    // SETTERS CON VALIDACIÓN
    // ==============================

    public void setId(Short id) {
        if (id == null || id <= 0)
            throw new IllegalArgumentException("El ID de la CCAA debe ser un número positivo.");
        this.id = id;
    }

    public void setDenominacion(String denominacion) {
        if (denominacion == null || denominacion.isBlank())
            throw new IllegalArgumentException("La denominación de la CCAA no puede estar vacía.");
        if (denominacion.length() > 30)
            throw new IllegalArgumentException("La denominación no puede superar los 30 caracteres.");
        this.denominacion = denominacion.trim();
    }

    public void setExtCode(BigDecimal extCode) {
        if (extCode == null)
            throw new IllegalArgumentException("El código externo no puede ser nulo.");
        if (extCode.scale() != 0) // verifica que sea un entero
            throw new IllegalArgumentException("El código externo debe ser un número entero.");
        if (extCode.compareTo(BigDecimal.ZERO) < 0 || extCode.compareTo(BigDecimal.valueOf(99)) > 0)
            throw new IllegalArgumentException("El código externo debe estar entre 0 y 99.");

        this.extCode = extCode;
    }


    // ==============================
    // MÉTODOS COMUNES
    // ==============================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CA CA)) return false;
        return Objects.equals(id, CA.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CCAA{" +
                "id=" + id +
                ", denominacion='" + denominacion + '\'' +
                ", extCode=" + extCode +
                '}';
    }
}
