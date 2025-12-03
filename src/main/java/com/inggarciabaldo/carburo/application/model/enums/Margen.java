package com.inggarciabaldo.carburo.application.model.enums;


import lombok.Getter;

@Getter
public enum Margen {
    DERECHO("D", "Derecho"),
    IZQUIERDO("I", "Izquierdo"),
    NO_APLICA("N", "No aplica"); // valor por defecto

    //Atributos
    private final String code;
    private final String description;

    Margen(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Devuelve el enumerado a partir del código, por defecto DERECHA si no se encuentra.
     */
    public static Margen fromCode(String code) {
        if (code == null)
            throw new IllegalArgumentException("Código de Margen nulo.");
        for (Margen margin : values()) {
            if (margin.code.equalsIgnoreCase(code)) {
                return margin;
            }
        }
        throw new IllegalArgumentException("Código de Margen no encontrado: " + code);
    }
}