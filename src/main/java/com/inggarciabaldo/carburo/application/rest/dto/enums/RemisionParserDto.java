package com.inggarciabaldo.carburo.application.rest.dto.enums;

import com.google.gson.annotations.SerializedName;
import com.inggarciabaldo.carburo.application.model.enums.Remision;
import lombok.Getter;

import static com.inggarciabaldo.carburo.config.parser.api.ResponseKeys.API_KEY_RESP_REMISION_DM;
import static com.inggarciabaldo.carburo.config.parser.api.ResponseKeys.API_KEY_RESP_REMISION_OM;

@Getter
public enum RemisionParserDto {
	@SerializedName(API_KEY_RESP_REMISION_OM) OM(Remision.OM),
	@SerializedName(API_KEY_RESP_REMISION_DM) DM(Remision.DM);

	//Atributos
	private final Remision relacionModelo;

	RemisionParserDto(Remision remision) {
		this.relacionModelo = remision;
	}
}