package app.carburo.loader.application.rest.dto.enums;

import com.google.gson.annotations.SerializedName;
import app.carburo.loader.application.model.enums.Remision;
import lombok.Getter;

import static app.carburo.loader.config.parser.api.ResponseKeys.API_KEY_RESP_REMISION_DM;
import static app.carburo.loader.config.parser.api.ResponseKeys.API_KEY_RESP_REMISION_OM;

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