package com.inggarciabaldo.carburo.parser.dto.EETT;

import com.google.gson.annotations.SerializedName;
import com.inggarciabaldo.carburo.model.enums.Margen;
import com.inggarciabaldo.carburo.model.enums.Remision;
import com.inggarciabaldo.carburo.model.enums.Venta;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
/**
 * DTO para la petición Estacion de Servicio (EESS).
 * Pensado para ser usado en peticiones de EESS.
 */
public class ESParserDTO {

    @SerializedName("IDEESS")
    private int ideess;

    @SerializedName("Rótulo")
    private String rotulo;

    @SerializedName("Dirección")
    private String direccion;

    @SerializedName("Horario")
    private String horario;

    @SerializedName("Localidad")
    private String localidad;

    @SerializedName("Municipio")
    private String municipio;

    @SerializedName("C.P.")
    private int cp;

    @SerializedName("Provincia")
    private String provincia;

    @SerializedName("IDMunicipio")
    private int idMunicipio;

    @SerializedName("IDProvincia")
    private int idProvincia;

    @SerializedName("IDCCAA")
    private int idCCAA;

    @SerializedName("Margen")
    private Margen margen;

    @SerializedName("Remisión")
    private Remision remision;

    @SerializedName("Tipo Venta")
    private Venta tipoVenta;

    @SerializedName("Latitud")
    private String latitud; // String por la coma, luego puedes convertir a double

    @SerializedName("Longitud (WGS84)")
    private String longitud; // idem

    @SerializedName("Precio Gasoleo A")
    private String precioGasoleoA;

    @SerializedName("Precio Gasolina 95 E5")
    private String precioGasolina95E5;

    @SerializedName("Precio Gasolina 98 E5")
    private String precioGasolina98E5;

    @SerializedName("% BioEtanol")
    private String bioEtanol;

    @SerializedName("% Éster metílico")
    private String esterMetilico;
}
