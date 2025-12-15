package com.inggarciabaldo.carburo.application.rest.dto;

import com.google.gson.annotations.SerializedName;
import com.inggarciabaldo.carburo.application.rest.dto.enums.MargenParserDto;
import com.inggarciabaldo.carburo.application.rest.dto.enums.RemisionParserDto;
import com.inggarciabaldo.carburo.application.rest.dto.enums.VentaParserDto;
import lombok.*;

import static com.inggarciabaldo.carburo.config.parser.api.ResponseKeys.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
/**
 * DTO para la petición Estacion de Servicio (EESS).
 * Pensado para ser usado en peticiones de EESS.
 */ public class ESParserDTO {

	@SerializedName(API_KEY_RESP_EESS_ID)
	private int ideess;

	@SerializedName(API_KEY_RESP_EESS_ROTULO)
	private String rotulo;

	@SerializedName(API_KEY_RESP_EESS_DIRECCION)
	private String direccion;

	@SerializedName(API_KEY_RESP_EESS_HORARIO)
	private String horario;

	@SerializedName(API_KEY_RESP_EESS_LOCALIDAD)
	private String localidad;

	@SerializedName(API_KEY_RESP_MUNICIPIO_DENOMINACION)
	private String municipio;

	@SerializedName(API_KEY_RESP_EESS_CP)
	private int cp;

	@SerializedName(API_KEY_RESP_PROVINCIA_DENOMINACION)
	private String provincia;

	@SerializedName(API_KEY_RESP_MUNICIPIO_ID)
	private int idMunicipio;

	@SerializedName(API_KEY_RESP_PROVINCIA_ID)
	private int idProvincia;

	@SerializedName(API_KEY_RESP_CA_ID)
	private int idCCAA;

	@SerializedName(API_KEY_RESP_EESS_MARGEN)
	private MargenParserDto margen;

	@SerializedName(API_KEY_RESP_EESS_REMISION)
	private RemisionParserDto remision;

	@SerializedName(API_KEY_RESP_EESS_VENTA)
	private VentaParserDto tipoVenta;

	@SerializedName(API_KEY_RESP_EESS_LATITUD)
	private String latitud; // String por la coma, luego puedes convertir a double

	@SerializedName(API_KEY_RESP_EESS_LONGITUD)
	private String longitud; // idem

	@SerializedName(API_KEY_RESP_EESS_X_100_BIOETANOL)
	private String bioEtanol;

	@SerializedName(API_KEY_RESP_EESS_X_100_ESTER_METILICO)
	private String esterMetilico;

	/*
	 * Precios
	 */
	private PreciosCombustibleParserDTO precios;
}
