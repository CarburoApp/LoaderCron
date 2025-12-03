package com.inggarciabaldo.carburo.application.rest.dto.enums;


import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public enum MargenParserDto {
    @SerializedName("D")
    DERECHO("D", "Derecho"),
    @SerializedName("I")
    IZQUIERDO("I", "Izquierdo"),
    @SerializedName("N")
    NO_APLICA("N", "No aplica"); // valor por defecto

    //Atributos
    private final String code;
    private final String description;

    MargenParserDto(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Devuelve el enumerado a partir del código, por defecto DERECHA si no se encuentra.
     */
    public static MargenParserDto fromCode(String code) {
        if (code == null)
            throw new IllegalArgumentException("Código de Margen nulo.");
        for (MargenParserDto margin : values()) {
            if (margin.code.equalsIgnoreCase(code)) {
                return margin;
            }
        }
        throw new IllegalArgumentException("Código de Margen no encontrado: " + code);
    }
}