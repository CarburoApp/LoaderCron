package com.inggarciabaldo.carburo.application.rest.dto.enums;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public enum RemisionDto {
    @SerializedName("om")
    OM("OM", "Datos procedentes del operador mayorista"),
    @SerializedName("dm")
    DM("DM", "Datos procedentes del distribuidor minorista"); // valor por defecto

    private final String code;
    private final String description;

    RemisionDto(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Devuelve el enumerado a partir del código.
     *
     * @throws IllegalArgumentException si el código es nulo o no existe.
     */
    public static RemisionDto fromCode(String code) {
        if (code == null || code.isEmpty())
            throw new IllegalArgumentException("Código de Remisión nulo o vació.");
        for (RemisionDto rem : values()) {
            if (rem.code.equalsIgnoreCase(code)) {
                return rem;
            }
        }
        throw new IllegalArgumentException("Código de Remisión no encontrado: '" + code + "'");
    }
}