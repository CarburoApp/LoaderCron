package com.inggarciabaldo.carburo.parser.dto.EETT;

import com.google.gson.annotations.SerializedName;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
/**
 * DTO para la petición de Estaciones de Servicio (EETT).
 * Pensado para ser usado en todas peticiones relacionadas con Estaciones Terrestres.
 */
public class PreciosCarburanteParserDTO {

    @SerializedName("Precio Adblue")
    private String precioAdblue;

    @SerializedName("Precio Amoniaco")
    private String precioAmoniaco;

    @SerializedName("Precio Biodiesel")
    private String precioBiodiesel;

    @SerializedName("Precio Bioetanol")
    private String precioBioetanol;

    @SerializedName("Precio Biogas Natural Comprimido")
    private String precioBiogasNaturalComprimido;

    @SerializedName("Precio Biogas Natural Licuado")
    private String precioBiogasNaturalLicuado;

    @SerializedName("Precio Diésel Renovable")
    private String precioDieselRenovable;

    @SerializedName("Precio Gas Natural Comprimido")
    private String precioGasNaturalComprimido;

    @SerializedName("Precio Gas Natural Licuado")
    private String precioGasNaturalLicuado;

    @SerializedName("Precio Gases licuados del petróleo")
    private String precioGLP;

    @SerializedName("Precio Gasoleo A")
    private String precioGasoleoA;

    @SerializedName("Precio Gasoleo B")
    private String precioGasoleoB;

    @SerializedName("Precio Gasoleo Premium")
    private String precioGasoleoPremium;

    @SerializedName("Precio Gasolina 95 E10")
    private String precioGasolina95E10;

    @SerializedName("Precio Gasolina 95 E25")
    private String precioGasolina95E25;

    @SerializedName("Precio Gasolina 95 E5")
    private String precioGasolina95E5;

    @SerializedName("Precio Gasolina 95 E5 Premium")
    private String precioGasolina95E5Premium;

    @SerializedName("Precio Gasolina 95 E85")
    private String precioGasolina95E85;

    @SerializedName("Precio Gasolina 98 E10")
    private String precioGasolina98E10;

    @SerializedName("Precio Gasolina 98 E5")
    private String precioGasolina98E5;

    @SerializedName("Precio Gasolina Renovable")
    private String precioGasolinaRenovable;

    @SerializedName("Precio Hidrogeno")
    private String precioHidrogeno;

    @SerializedName("Precio Metanol")
    private String precioMetanol;
}
