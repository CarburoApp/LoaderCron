package com.inggarciabaldo.carburo.application.model.enums;

import com.inggarciabaldo.carburo.application.model.enums.fromcode.FromCode.GetCodeEnumInterface;
import lombok.Getter;

@Getter
public enum Venta implements GetCodeEnumInterface {
	PUBLICA("P", "Venta al público en general"),
	RESTRINGIDA("R", "Venta restringida a socios o cooperativistas");

	private final String code;
	private final String description;

	Venta(String code, String description) {
		this.code        = code;
		this.description = description;
	}
}