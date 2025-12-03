package com.inggarciabaldo.carburo.application.rest.dto.enums;

import com.google.gson.annotations.SerializedName;
import com.inggarciabaldo.carburo.application.model.enums.Remision;
import lombok.Getter;

@Getter
public enum RemisionParserDto {
	@SerializedName("om") OM(Remision.OM),
	@SerializedName("dm") DM(Remision.DM);

	//Atributos
	private final Remision relacionModelo;

	RemisionParserDto(Remision remision) {
		this.relacionModelo = remision;
	}
}