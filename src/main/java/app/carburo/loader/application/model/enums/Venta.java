package app.carburo.loader.application.model.enums;

import app.carburo.loader.application.model.enums.fromcode.FromCode.GetCodeEnumInterface;
import lombok.Getter;

@Getter
public enum Venta implements GetCodeEnumInterface {
	PUBLICA("PUBLICA", "Venta al público en general"),
	RESTRINGIDA("RESTRINGIDA", "Venta restringida a socios o cooperativistas");

	private final String code;
	private final String description;

	Venta(String code, String description) {
		this.code        = code;
		this.description = description;
	}
}