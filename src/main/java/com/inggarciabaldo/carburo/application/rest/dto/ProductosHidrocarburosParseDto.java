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
 * DTO para los Productos Petrolíferos.
 * Pensado para ser usado en peticiones de listados de Productos Petrolíferos disponibles desde la API de carburantes.
 */
public class ProductosHidrocarburosParseDto {

    @SerializedName("IDProducto")
    private int idProducto;

    @SerializedName("NombreProducto")
    private String NombreProducto;

    @SerializedName("NombreProductoAbreviatura")
    private String abreviaturaNombreProducto;
}
