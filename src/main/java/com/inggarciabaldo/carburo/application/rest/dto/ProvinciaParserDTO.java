package com.inggarciabaldo.carburo.parser.dto.listados;

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

