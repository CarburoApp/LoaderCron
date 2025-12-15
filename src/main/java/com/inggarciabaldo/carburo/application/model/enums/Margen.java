package com.inggarciabaldo.carburo.application.model.enums;

import com.inggarciabaldo.carburo.application.model.enums.fromcode.FromCode.GetCodeEnumInterface;
import lombok.Getter;

@Getter
public enum Margen implements GetCodeEnumInterface {
    DERECHO("DERECHO", "Derecho"),
    IZQUIERDO("IZQUIERDO", "Izquierdo"),
    NO_APLICA("NO_APLICA", "No aplica"); // valor por defecto

    //Atributos
    private final String code;
    private final String description;

    Margen(String code, String description) {
        this.code = code;
        this.description = description;
    }
}