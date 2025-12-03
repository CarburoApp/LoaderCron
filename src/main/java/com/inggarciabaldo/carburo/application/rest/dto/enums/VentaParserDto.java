package com.inggarciabaldo.carburo.application.rest.dto.enums;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public enum VentaDto {
    @SerializedName("P")
    PUBLICA("P", "Venta al público en general"),
    @SerializedName("R")
    RESTRINGIDA("R", "Venta restringida a socios o cooperativistas");

    private final String code;
    private final String description;

    VentaDto(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Devuelve el enumerado a partir del código, por defecto RESTRINGIDA si no se encuentra.
     */
    public static VentaDto fromCode(String code) {
        if (code == null || code.isEmpty())
            throw new IllegalArgumentException("Código de Tipo de venta nulo o vació.");
        for (VentaDto sale : values()) {
            if (sale.code.equalsIgnoreCase(code)) {
                return sale;
            }
        }
        throw new IllegalArgumentException("Código de tipo de Venta no encontrado: " + code);
    }
}