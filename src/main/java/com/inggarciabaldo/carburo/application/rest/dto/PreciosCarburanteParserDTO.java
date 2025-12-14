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
/**
 * DTO para la petición de Estaciones de Servicio (EETT).
 * Pensado para ser usado en todas peticiones relacionadas con Estaciones Terrestres.
 */
public class PreciosCarburanteParserDTO {

    @SerializedName(API_KEY_RESP_PRECIO_ADBLUE)
    private String precioAdblue;

    @SerializedName(API_KEY_RESP_PRECIO_AMONIACO)
    private String precioAmoniaco;

    @SerializedName(API_KEY_RESP_PRECIO_BIODIESEL)
    private String precioBiodiesel;

    @SerializedName(API_KEY_RESP_PRECIO_BIOETANOL)
    private String precioBioetanol;

    @SerializedName(API_KEY_RESP_PRECIO_BIOGAS_NATURAL_COMPRIMIDO)
    private String precioBiogasNaturalComprimido;

    @SerializedName(API_KEY_RESP_PRECIO_BIOGAS_NATURAL_LIQUIDO)
    private String precioBiogasNaturalLicuado;

    @SerializedName(API_KEY_RESP_PRECIO_DIESEL_RENOVABLE)
    private String precioDieselRenovable;

    @SerializedName(API_KEY_RESP_PRECIO_GAS_NATURAL_COMPRIMIDO)
    private String precioGasNaturalComprimido;

    @SerializedName(API_KEY_RESP_PRECIO_GAS_NATURAL_LIQUIDO)
    private String precioGasNaturalLicuado;

    @SerializedName(API_KEY_RESP_PRECIO_GLP)
    private String precioGLP;

    @SerializedName(API_KEY_RESP_PRECIO_GASOLEO_A)
    private String precioGasoleoA;

    @SerializedName(API_KEY_RESP_PRECIO_GASOLEO_B)
    private String precioGasoleoB;

    @SerializedName(API_KEY_RESP_PRECIO_GASOLEO_PREMIUM)
    private String precioGasoleoPremium;

    @SerializedName(API_KEY_RESP_PRECIO_GASOLINA_95_E10)
    private String precioGasolina95E10;

    @SerializedName(API_KEY_RESP_PRECIO_GASOLINA_95_E25)
    private String precioGasolina95E25;

    @SerializedName(API_KEY_RESP_PRECIO_GASOLINA_95_E5)
    private String precioGasolina95E5;

    @SerializedName(API_KEY_RESP_PRECIO_GASOLINA_95_E5_PREMIUM)
    private String precioGasolina95E5Premium;

    @SerializedName(API_KEY_RESP_PRECIO_GASOLINA_95_E85)
    private String precioGasolina95E85;

    @SerializedName(API_KEY_RESP_PRECIO_GASOLINA_98_E10)
    private String precioGasolina98E10;

    @SerializedName(API_KEY_RESP_PRECIO_GASOLINA_98_E5)
    private String precioGasolina98E5;

    @SerializedName(API_KEY_RESP_PRECIO_GASOLINA_RENOVABLE)
    private String precioGasolinaRenovable;

    @SerializedName(API_KEY_RESP_PRECIO_HIDROGENO)
    private String precioHidrogeno;

    @SerializedName(API_KEY_RESP_PRECIO_METANOL)
    private String precioMetanol;
}
