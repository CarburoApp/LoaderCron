package com.inggarciabaldo.carburo.parser.dto.listados;

import com.google.gson.annotations.SerializedName;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
/**
 * DTO para las Comunidades Autónomas (CCAA).
 * Pensado para ser usado en peticiones de listados de CCAA desde la API de carburantes.
 */
public class CAParserDTO {

    @SerializedName("IDCCAA")
    private int idCA;

    @SerializedName("CCAA")
    private String CA;
}

