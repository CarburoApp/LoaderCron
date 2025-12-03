package com.inggarciabaldo.carburo.application.rest.dto;

import com.google.gson.annotations.SerializedName;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ProvinciaParserDTO {

    @SerializedName("IDProvincia")
    private int idProvincia;

    @SerializedName("IDCCAA")
    private int idCCAA;

    @SerializedName("Provincia")
    private String provincia;

    @SerializedName("CCAA")
    private String ccaa;
}

