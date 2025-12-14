package com.inggarciabaldo.carburo.application.rest.dto;

import com.google.gson.annotations.SerializedName;import static com.inggarciabaldo.carburo.config.parser.api.ResponseKeys.*;
import lombok.*;

/**
 * DTO para los Productos Petrolíferos.
 * Pensado para ser usado en peticiones de listados de Productos Petrolíferos disponibles desde la API de carburantes.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ProductosHidrocarburosParseDto {

    @SerializedName(API_KEY_RESP_PRODUCTO_ID)
    private int idProducto;

    @SerializedName(API_KEY_RESP_PRODUCTO_DENOMINACION)
    private String nombreProducto;

    @SerializedName(API_KEY_RESP_PRODUCTO_ABREVIATURA)
    private String abreviaturaNombreProducto;
}
