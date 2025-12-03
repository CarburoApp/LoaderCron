package com.inggarciabaldo.carburo.application.rest.dto.enums;


import com.google.gson.annotations.SerializedName;
import com.inggarciabaldo.carburo.application.model.enums.Margen;
import lombok.Getter;

@Getter
public enum MargenParserDto {
	@SerializedName("D") DERECHO(Margen.DERECHO),
	@SerializedName("I") IZQUIERDO(Margen.IZQUIERDO),
	@SerializedName("N") NO_APLICA(Margen.NO_APLICA);

	//Atributos
	private final Margen relacionModelo;

	MargenParserDto(Margen margen) {
		this.relacionModelo = margen;
	}
}