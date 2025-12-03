package com.inggarciabaldo.carburo.application.model.enums;

import lombok.Getter;

@Getter
public enum Venta {
	PUBLICA("P", "Venta al público en general"),
	RESTRINGIDA("R", "Venta restringida a socios o cooperativistas");

	private final String code;
	private final String description;

	Venta(String code, String description) {
		this.code        = code;
		this.description = description;
	}

	/**
	 * Devuelve el enumerado a partir del código, por defecto RESTRINGIDA si no se encuentra.
	 */
	public static Venta fromCode(String code) {
		if (code == null || code.isEmpty())
			throw new IllegalArgumentException("Código de Tipo de venta nulo o vació.");
		for (Venta sale : values()) {
			if (sale.code.equalsIgnoreCase(code)) {
				return sale;
			}
		}
		throw new IllegalArgumentException(
				"Código de tipo de Venta no encontrado: " + code);
	}
}