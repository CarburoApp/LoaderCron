package com.inggarciabaldo.carburo.application.rest.dto;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.util.List;

import static com.inggarciabaldo.carburo.config.parser.api.ResponseKeys.*;


/**
 * DTO para la petición base de Estaciones Terrestres (EETT).
 * Pensado para ser usado en todas peticiones relacionadas con Estaciones Terrestres.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class EETTReqResParserDTO {

    @SerializedName(API_KEY_RESP_FECHA)
    private String fecha; // parsearemos a LocalDateTime más adelante si quieres

    @SerializedName(API_KEY_RESP_LISTADO_EESS)
    private List<ESParserDTO> listaEESS;

    @SerializedName(API_KEY_RESP_NOTA)
    private String nota;

    @SerializedName(API_KEY_RESP_RES_CONSULTA)
    private String resultadoConsulta;
}
