package com.inggarciabaldo.carburo.parser.dto.EETT;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
/**
 * DTO para la petición base de Estaciones Terrestres (EETT).
 * Pensado para ser usado en todas peticiones relacionadas con Estaciones Terrestres.
 */
public class EETTReqResParserDTO {

    @SerializedName("Fecha")
    private String fecha; // parsearemos a LocalDateTime más adelante si quieres

    @SerializedName("ListaEESSPrecio")
    private List<ESParserDTO> listaEESS;

    @SerializedName("Nota")
    private String nota;

    @SerializedName("ResultadoConsulta")
    private String resultadoConsulta;
}
