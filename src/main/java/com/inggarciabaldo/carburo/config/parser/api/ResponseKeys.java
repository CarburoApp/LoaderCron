package com.inggarciabaldo.carburo.config.parser.api;

public abstract class ResponseKeys {

	// Constructor provado para evitar instancias innecesarias.
	private ResponseKeys(){}

	// Claves comunes en las respuesta base de la API

	public static final String API_KEY_RESP_FECHA = "Fecha";
	public static final String API_KEY_RESP_LISTADO_EESS = "ListaEESSPrecio";
	public static final String API_KEY_RESP_NOTA = "Nota";
	public static final String API_KEY_RESP_RES_CONSULTA = "ResultadoConsulta";
	public static final String API_KEY_RESP_RES_CONSULTA_OK = "OK";

	// Claves específicas para cada EESS del listado

	public static final String API_KEY_RESP_EESS_ID = "IDEESS";
	public static final String API_KEY_RESP_EESS_ROTULO = "Rótulo";
	public static final String API_KEY_RESP_EESS_DIRECCION = "Dirección";
	public static final String API_KEY_RESP_EESS_HORARIO = "Horario";
	public static final String API_KEY_RESP_EESS_LOCALIDAD = "Localidad";
	public static final String API_KEY_RESP_EESS_CP = "C.P.";
	public static final String API_KEY_RESP_EESS_MARGEN = "Margen";
	public static final String API_KEY_RESP_EESS_REMISION = "Remisión";
	public static final String API_KEY_RESP_EESS_VENTA = "Tipo Venta";
	public static final String API_KEY_RESP_EESS_LATITUD = "Latitud";
	public static final String API_KEY_RESP_EESS_LONGITUD = "Longitud (WGS84)";
	public static final String API_KEY_RESP_EESS_X_100_BIOETANOL = "% BioEtanol";
	public static final String API_KEY_RESP_EESS_X_100_ESTER_METILICO = "% Éster metílico";

	// Claves específicas para las CCAA

	public static final String API_KEY_RESP_CA_ID = "IDCCAA";
	public static final String API_KEY_RESP_CA_DENOMINACION = "CCAA";

	// Claves específicas para las Provincas

	public static final String API_KEY_RESP_PROVINCIA_ID = "IDProvincia";
	public static final String API_KEY_RESP_PROVINCIA_DENOMINACION = "Provincia";

	// Claves específicas para los Municipios

	public static final String API_KEY_RESP_MUNICIPIO_ID = "IDMunicipio";
	public static final String API_KEY_RESP_MUNICIPIO_DENOMINACION = "Municipio";

	// Claves específicas para los Productos Petrolíferos

	public static final String API_KEY_RESP_PRODUCTO_ID = "IDProducto";
	public static final String API_KEY_RESP_PRODUCTO_DENOMINACION = "NombreProducto";
	public static final String API_KEY_RESP_PRODUCTO_ABREVIATURA = "NombreProductoAbreviatura";

	// Claves para los precios de combustibles
	public static final String API_KEY_RESP_PRECIO_PRODUCTO = "Precio";
	public static final String API_KEY_RESP_PRECIO_ADBLUE = "Precio Adblue";
	public static final String API_KEY_RESP_PRECIO_AMONIACO = "Precio Amoniaco";
	public static final String API_KEY_RESP_PRECIO_BIODIESEL = "Precio Biodiesel";
	public static final String API_KEY_RESP_PRECIO_BIOETANOL = "Precio Bioetanol";
	public static final String API_KEY_RESP_PRECIO_BIOGAS_NATURAL_COMPRIMIDO = "Precio Biogas Natural Comprimido";
	public static final String API_KEY_RESP_PRECIO_BIOGAS_NATURAL_LIQUIDO = "Precio Biogas Natural Licuado";
	public static final String API_KEY_RESP_PRECIO_DIESEL_RENOVABLE = "Precio Diésel Renovable";
	public static final String API_KEY_RESP_PRECIO_GAS_NATURAL_COMPRIMIDO = "Precio Gas Natural Comprimido";
	public static final String API_KEY_RESP_PRECIO_GAS_NATURAL_LIQUIDO = "Precio Gas Natural Licuado";
	public static final String API_KEY_RESP_PRECIO_GLP = "Precio Gases licuados del petróleo";
	public static final String API_KEY_RESP_PRECIO_GASOLEO_A = "Precio Gasoleo A";
	public static final String API_KEY_RESP_PRECIO_GASOLEO_B = "Precio Gasoleo B";
	public static final String API_KEY_RESP_PRECIO_GASOLEO_PREMIUM = "Precio Gasoleo Premium";
	public static final String API_KEY_RESP_PRECIO_GASOLINA_95_E10 = "Precio Gasolina 95 E10";
	public static final String API_KEY_RESP_PRECIO_GASOLINA_95_E25 = "Precio Gasolina 95 E25";
	public static final String API_KEY_RESP_PRECIO_GASOLINA_95_E5 = "Precio Gasolina 95 E5";
	public static final String API_KEY_RESP_PRECIO_GASOLINA_95_E5_PREMIUM = "Precio Gasolina 95 E5 Premium";
	public static final String API_KEY_RESP_PRECIO_GASOLINA_95_E85 = "Precio Gasolina 95 E85";
	public static final String API_KEY_RESP_PRECIO_GASOLINA_98_E10 = "Precio Gasolina 98 E10";
	public static final String API_KEY_RESP_PRECIO_GASOLINA_98_E5 = "Precio Gasolina 98 E5";
	public static final String API_KEY_RESP_PRECIO_GASOLINA_RENOVABLE = "Precio Gasolina Renovable";
	public static final String API_KEY_RESP_PRECIO_HIDROGENO = "Precio Hidrogeno";
	public static final String API_KEY_RESP_PRECIO_METANOL = "Precio Metanol";

	// Claves específicas para los tipos de Venta
	public static final String API_KEY_RESP_VENTA_PUBLICA = "P";
	public static final String API_KEY_RESP_VENTA_RESTRINGIDA = "R";

	// Claves específicas para los tipos de remisión
	public static final String API_KEY_RESP_REMISION_OM = "om";
	public static final String API_KEY_RESP_REMISION_DM = "dm";

	// Claves específicas para los tipos de Margen
	public static final String API_KEY_RESP_MARGEN_IZQUIERDO = "I";
	public static final String API_KEY_RESP_MARGEN_DERECHO = "D";
	public static final String API_KEY_RESP_MARGEN_NO_APLICA = "N";
}
