package com.inggarciabaldo.carburo.application.rest.dto;

import com.google.gson.annotations.SerializedName;
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

    @SerializedName("IDProducto")
    private int idProducto;

    @SerializedName("NombreProducto")
    private String nombreProducto;

    @SerializedName("NombreProductoAbreviatura")
    private String abreviaturaNombreProducto;
}
