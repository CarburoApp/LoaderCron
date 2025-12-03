package com.inggarciabaldo.carburo.application.rest.dto;

import com.google.gson.annotations.SerializedName;
import com.inggarciabaldo.carburo.application.rest.dto.enums.MargenParserDto;
import com.inggarciabaldo.carburo.application.rest.dto.enums.RemisionParserDto;
import com.inggarciabaldo.carburo.application.rest.dto.enums.VentaParserDto;
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
 */ public class ESParserDTO {

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
	private MargenParserDto margen;

	@SerializedName("Remisión")
	private RemisionParserDto remision;

	@SerializedName("Tipo Venta")
	private VentaParserDto tipoVenta;

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
