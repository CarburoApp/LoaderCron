package com.inggarciabaldo.carburo.application.model.enums;

import com.inggarciabaldo.carburo.application.model.enums.fromcode.FromCode.GetCodeEnumInterface;
import lombok.Getter;

@Getter
public enum Remision implements GetCodeEnumInterface {
    OM("OM", "Datos procedentes del operador mayorista"),
    DM("DM", "Datos procedentes del distribuidor minorista"); // valor por defecto

    private final String code;
    private final String description;

    Remision(String code, String description) {
        this.code = code;
        this.description = description;
    }
}