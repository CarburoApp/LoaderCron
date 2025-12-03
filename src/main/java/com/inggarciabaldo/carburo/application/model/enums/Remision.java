package com.inggarciabaldo.carburo.application.model.enums;

import lombok.Getter;

@Getter
public enum Remision {
    OM("OM", "Datos procedentes del operador mayorista"),
    DM("DM", "Datos procedentes del distribuidor minorista"); // valor por defecto

    private final String code;
    private final String description;

    Remision(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Devuelve el enumerado a partir del código.
     *
     * @throws IllegalArgumentException si el código es nulo o no existe.
     */
    public static Remision fromCode(String code) {
        if (code == null || code.isEmpty())
            throw new IllegalArgumentException("Código de Remisión nulo o vació.");
        for (Remision rem : values()) {
            if (rem.code.equalsIgnoreCase(code)) {
                return rem;
            }
        }
        throw new IllegalArgumentException("Código de Remisión no encontrado: '" + code + "'");
    }
}