package com.inggarciabaldo.carburo.model.entity.eess;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad que representa el precio de un combustible en una EESS (estación de servicio)
 * en una fecha determinada.
 */
@Entity
@Table(name = "PrecioCombustible")
@Getter
@NoArgsConstructor
public class PrecioCombustible implements Serializable {

    // ==============================
    // CLAVE COMPUESTA
    // ==============================
    @EmbeddedId
    private PrecioCombustibleId id;

    // ==============================
    // RELACIONES
    // ==============================
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idCombustible")
    @JoinColumn(name = "id_combustible", nullable = false)
    private Combustible combustible;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idEess")
    @JoinColumn(name = "id_eess", nullable = false)
    private ES ES;

    // ==============================
    // ATRIBUTOS PROPIOS
    // ==============================
    @Column(nullable = false, precision = 6, scale = 3)
    private BigDecimal precio;


    // ==============================
    // CONSTRUCTOR COMPLETO
    // ==============================
    public PrecioCombustible(ES ES, Combustible combustible, LocalDate fecha, BigDecimal precio) {
        if (ES == null || combustible == null)
            throw new IllegalArgumentException("EESS y Combustible no pueden ser nulos");
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("El precio debe ser mayor que 0");
        if (fecha == null || fecha.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Fecha inválida");

        this.ES = ES;
        this.combustible = combustible;
        this.precio = precio.setScale(3, RoundingMode.HALF_UP);
        this.id = new PrecioCombustibleId(ES.getId(), combustible.getId(), fecha);
    }

    // ==============================
    // SETTERS CON VALIDACIÓN
    // ==============================
    public void setPrecio(BigDecimal precio) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("El precio debe ser mayor que 0");
        this.precio = precio.setScale(3, RoundingMode.HALF_UP);
    }

    // ==============================
    // MÉTODOS COMUNES
    // ==============================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrecioCombustible that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PrecioCombustible{" +
                "EESS=" + (ES != null ? ES.getId() : null) +
                ", Combustible=" + (combustible != null ? combustible.getId() : null) +
                ", fecha=" + id.getFecha() +
                ", precio=" + precio +
                '}';
    }
}
