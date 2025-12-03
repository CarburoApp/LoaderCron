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
 * DTO para los Municipios.
 * Pensado para ser usado en peticiones de listados de Municipios desde la API de carburantes.
 */
public class MunicipioParserDTO {

    @SerializedName("IDMunicipio")
    private int idMunicipio;

    @SerializedName("IDProvincia")
    private int idProvincia;

    @SerializedName("IDCCAA")
    private int idComunidadAutonoma;

    @SerializedName("Municipio")
    private String municipio;

    @SerializedName("Provincia")
    private String provincia;

    @SerializedName("CCAA")
    private String comunidadAutonoma;
}

