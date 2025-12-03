package com.inggarciabaldo.carburo.application.rest.dto;

import com.google.gson.annotations.SerializedName;
import lombok.*;

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

	@SerializedName("IDCCAA")
	private int idCA;

	@SerializedName("CCAA")
	private String ca;
}

