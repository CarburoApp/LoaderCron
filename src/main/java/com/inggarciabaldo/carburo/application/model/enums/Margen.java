package com.inggarciabaldo.carburo.application.model.enums;

import com.inggarciabaldo.carburo.application.model.enums.fromcode.FromCode.GetCodeEnumInterface;
import lombok.Getter;

@Getter
public enum Margen implements GetCodeEnumInterface {
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
}