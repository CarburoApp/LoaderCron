package com.inggarciabaldo.carburo.config.parser.api;

public interface ResponseKeys {

	// Claves comunes en las respuesta base de la API

	String API_KEY_RESP_FECHA = "Fecha";
	String API_KEY_RESP_LISTADO_EESS = "ListaEESSPrecio";
	String API_KEY_RESP_NOTA = "Nota";
	String API_KEY_RESP_RES_CONSULTA = "ResultadoConsulta";
	String API_KEY_RESP_RES_CONSULTA_OK = "OK";

	// Claves específicas para cada EESS del listado

	String API_KEY_RESP_EESS_ID = "IDEESS";
	String API_KEY_RESP_EESS_ROTULO = "Rótulo";
	String API_KEY_RESP_EESS_DIRECCION = "Dirección";
	String API_KEY_RESP_EESS_HORARIO = "Horario";
	String API_KEY_RESP_EESS_LOCALIDAD = "Localidad";
	String API_KEY_RESP_EESS_CP = "C.P.";
	String API_KEY_RESP_EESS_MARGEN = "Margen";
	String API_KEY_RESP_EESS_REMISION = "Remisión";
	String API_KEY_RESP_EESS_VENTA = "Tipo Venta";
	String API_KEY_RESP_EESS_LATITUD = "Latitud";
	String API_KEY_RESP_EESS_LONGITUD = "Longitud (WGS84)";
	String API_KEY_RESP_EESS_X_100_BIOETANOL = "% BiotaEnol";
	String API_KEY_RESP_EESS_X_100_ESTER_METILICO = "% Éster metílico";

	// Claves específicas para las CCAA

	String API_KEY_RESP_CA_ID = "IDCCAA";
	String API_KEY_RESP_CA_DENOMINACION = "CCAA";

	// Claves específicas para las Provincas

	String API_KEY_RESP_PROVINCIA_ID = "IDProvincia";
	String API_KEY_RESP_PROVINCIA_DENOMINACION = "Provincia";

	// Claves específicas para los Municipios

	String API_KEY_RESP_MUNICIPIO_ID = "IDMunicipio";
	String API_KEY_RESP_MUNICIPIO_DENOMINACION = "Municipio";

	// Claves específicas para los Productos Petrolíferos

	String API_KEY_RESP_PRODUCTO_ID = "IDProducto";
	String API_KEY_RESP_PRODUCTO_DENOMINACION = "NombreProducto";
	String API_KEY_RESP_PRODUCTO_ABREVIATURA = "NombreProductoAbreviatura";

	// Claves para los precios de combustibles
	String API_KEY_RESP_PRECIO_ADBLUE = "Precio Adblue";
	String API_KEY_RESP_PRECIO_AMONIACO = "Precio Amoniaco";
	String API_KEY_RESP_PRECIO_BIODIESEL = "Precio Biodiesel";
	String API_KEY_RESP_PRECIO_BIOETANOL = "Precio Bioetanol";
	String API_KEY_RESP_PRECIO_BIOGAS_NATURAL_COMPRIMIDO = "Precio Biogas Natural Comprimido";
	String API_KEY_RESP_PRECIO_BIOGAS_NATURAL_LIQUIDO = "Precio Biogas Natural Licuado";
	String API_KEY_RESP_PRECIO_DIESEL_RENOVABLE = "Precio Diésel Renovable";
	String API_KEY_RESP_PRECIO_GAS_NATURAL_COMPRIMIDO = "Precio Gas Natural Comprimido";
	String API_KEY_RESP_PRECIO_GAS_NATURAL_LIQUIDO = "Precio Gas Natural Licuado";
	String API_KEY_RESP_PRECIO_GLP = "Precio Gases licuados del petróleo";
	String API_KEY_RESP_PRECIO_GASOLEO_A = "Precio Gasoleo A";
	String API_KEY_RESP_PRECIO_GASOLEO_B = "Precio Gasoleo B";
	String API_KEY_RESP_PRECIO_GASOLEO_PREMIUM = "Precio Gasoleo Premium";
	String API_KEY_RESP_PRECIO_GASOLINA_95_E10 = "Precio Gasolina 95 E10";
	String API_KEY_RESP_PRECIO_GASOLINA_95_E25 = "Precio Gasolina 95 E25";
	String API_KEY_RESP_PRECIO_GASOLINA_95_E5 = "Precio Gasolina 95 E5";
	String API_KEY_RESP_PRECIO_GASOLINA_95_E5_PREMIUM = "Precio Gasolina 95 E5 Premium";
	String API_KEY_RESP_PRECIO_GASOLINA_95_E85 = "Precio Gasolina 95 E85";
	String API_KEY_RESP_PRECIO_GASOLINA_98_E10 = "Precio Gasolina 98 E10";
	String API_KEY_RESP_PRECIO_GASOLINA_98_E5 = "Precio Gasolina 98 E5";
	String API_KEY_RESP_PRECIO_GASOLINA_RENOVABLE = "Precio Gasolina Renovable";
	String API_KEY_RESP_PRECIO_HIDROGENO = "Precio Hidrogeno";
	String API_KEY_RESP_PRECIO_METANOL = "Precio Metanol";

	// Claves específicas para los tipos de Venta
	String API_KEY_RESP_VENTA_PUBLICA = "P";
	String API_KEY_RESP_VENTA_RESTRINGIDA = "R";

	// Claves específicas para los tipos de remisión
	String API_KEY_RESP_REMISION_OM = "om";
	String API_KEY_RESP_REMISION_DM = "dm";

	// Claves específicas para los tipos de Margen
	String API_KEY_RESP_MARGEN_IZQUIERDO = "I";
	String API_KEY_RESP_MARGEN_DERECHO = "D";
	String API_KEY_RESP_MARGEN_NO_APLICA = "N";


}
