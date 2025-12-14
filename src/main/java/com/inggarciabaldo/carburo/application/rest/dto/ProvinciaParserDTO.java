package com.inggarciabaldo.carburo.application.rest.dto;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import static com.inggarciabaldo.carburo.config.parser.api.ResponseKeys.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ProvinciaParserDTO {

    @SerializedName(API_KEY_RESP_PROVINCIA_ID)
    private int idProvincia;

    @SerializedName(API_KEY_RESP_CA_ID)
    private int idCCAA;

    @SerializedName(API_KEY_RESP_PROVINCIA_DENOMINACION)
    private String provincia;

    @SerializedName(API_KEY_RESP_CA_DENOMINACION)
    private String ccaa;
}

