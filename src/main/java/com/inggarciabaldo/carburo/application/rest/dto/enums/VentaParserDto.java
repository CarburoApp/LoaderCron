package com.inggarciabaldo.carburo.application.rest.dto.enums;

import com.google.gson.annotations.SerializedName;
import com.inggarciabaldo.carburo.application.model.enums.Venta;
import lombok.Getter;

@Getter
public enum VentaParserDto {
	@SerializedName("P") PUBLICA(Venta.PUBLICA),
	@SerializedName("R") RESTRINGIDA(Venta.RESTRINGIDA);

	//Atributos
	private final Venta relacionModelo;

	VentaParserDto(Venta venta) {
		this.relacionModelo = venta;
	}
}