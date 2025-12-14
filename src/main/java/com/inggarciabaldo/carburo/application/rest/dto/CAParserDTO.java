package com.inggarciabaldo.carburo.application.rest.dto;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import static com.inggarciabaldo.carburo.config.parser.api.ResponseKeys.*;

/**
 * DTO para las Comunidades Autónomas (CCAA).
 * Pensado para ser usado en peticiones de listados de CCAA desde la API de carburantes.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class CAParserDTO {

	@SerializedName(API_KEY_RESP_CA_ID)
	private int idCA;

	@SerializedName(API_KEY_RESP_CA_DENOMINACION)
	private String ca;
}

