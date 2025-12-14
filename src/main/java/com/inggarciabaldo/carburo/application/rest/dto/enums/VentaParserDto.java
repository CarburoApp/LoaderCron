package com.inggarciabaldo.carburo.application.rest.dto.enums;

import com.google.gson.annotations.SerializedName;
import com.inggarciabaldo.carburo.application.model.enums.Venta;
import lombok.Getter;

import static com.inggarciabaldo.carburo.config.parser.api.ResponseKeys.API_KEY_RESP_VENTA_PUBLICA;
import static com.inggarciabaldo.carburo.config.parser.api.ResponseKeys.API_KEY_RESP_VENTA_RESTRINGIDA;

@Getter
public enum VentaParserDto {
	@SerializedName(API_KEY_RESP_VENTA_PUBLICA) PUBLICA(Venta.PUBLICA),
	@SerializedName(API_KEY_RESP_VENTA_RESTRINGIDA) RESTRINGIDA(Venta.RESTRINGIDA);

	//Atributos
	private final Venta relacionModelo;

	VentaParserDto(Venta venta) {
		this.relacionModelo = venta;
	}
}