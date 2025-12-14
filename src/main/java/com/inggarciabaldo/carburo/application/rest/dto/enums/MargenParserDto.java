package com.inggarciabaldo.carburo.application.rest.dto.enums;


import com.google.gson.annotations.SerializedName;
import com.inggarciabaldo.carburo.application.model.enums.Margen;
import lombok.Getter;
import static com.inggarciabaldo.carburo.config.parser.api.ResponseKeys.*;

@Getter
public enum MargenParserDto {
	@SerializedName(API_KEY_RESP_MARGEN_DERECHO) DERECHO(Margen.DERECHO),
	@SerializedName(API_KEY_RESP_MARGEN_IZQUIERDO) IZQUIERDO(Margen.IZQUIERDO),
	@SerializedName(API_KEY_RESP_MARGEN_NO_APLICA) NO_APLICA(Margen.NO_APLICA);

	//Atributos
	private final Margen relacionModelo;

	MargenParserDto(Margen margen) {
		this.relacionModelo = margen;
	}
}