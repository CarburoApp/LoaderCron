package com.inggarciabaldo.carburo.config.persistencia;

/**
 * Constantes con los nombres de columnas de la base de datos.
 * Útiles para evitar errores de tipeo en consultas SQL y ResultSet.
 */
public final class DBColumns {

	// Constr. privado para evitar que se instancie
	private DBColumns() {}

	// =======================================================
	// Nombres de tablas
	// =======================================================
	public static final String CCAA_TABLE = "ccaa";
	public static final String PROVINCIA_TABLE = "provincia";
	public static final String MUNICIPIO_TABLE = "municipio";
	public static final String EESS_TABLE = "eess";
	public static final String COMBUSTIBLE_TABLE = "combustible";
	public static final String PRECIOCOMBUSTIBLE_TABLE = "preciocombustible";
	public static final String COMBUSTIBLE_DISPONIBLE_TABLE = "combustibledisponible";

	// =======================================================
	// CCAA
	// =======================================================
	public static final String CCAA_ID = "id";
	public static final String CCAA_DENOMINACION = "denominacion";
	public static final String CCAA_EXT_CODE = "ext_code";

	// =======================================================
	// PROVINCIA
	// =======================================================
	public static final String PROVINCIA_ID = "id";
	public static final String PROVINCIA_DENOMINACION = "denominacion";
	public static final String PROVINCIA_EXT_CODE = "ext_code";
	public static final String PROVINCIA_ID_CCAA = "id_ccaa";

	// =======================================================
	// MUNICIPIO
	// =======================================================
	public static final String MUNICIPIO_ID = "id";
	public static final String MUNICIPIO_DENOMINACION = "denominacion";
	public static final String MUNICIPIO_EXT_CODE = "ext_code";
	public static final String MUNICIPIO_ID_PROVINCIA = "id_provincia";

	// =======================================================
	// EESS
	// =======================================================
	public static final String EESS_ID = "id";
	public static final String EESS_EXT_CODE = "ext_code";
	public static final String EESS_ROTULO = "rotulo";
	public static final String EESS_HORARIO = "horario";
	public static final String EESS_DIRECCION = "direccion";
	public static final String EESS_LOCALIDAD = "localidad";
	public static final String EESS_CODIGO_POSTAL = "codigo_postal";
	public static final String EESS_ID_MUNICIPIO = "id_municipio";
	public static final String EESS_ID_PROVINCIA = "id_provincia";
	public static final String EESS_LATITUD = "latitud";
	public static final String EESS_LONGITUD = "longitud";
	public static final String EESS_REMISION = "remision";
	public static final String EESS_X100_BIO_ETANOL = "x100_bio_etanol";
	public static final String EESS_X100_ESTER_METILICO = "x100_ester_metilico";
	public static final String EESS_MARGEN = "margen";
	public static final String EESS_VENTA = "venta";

	// =======================================================
	// COMBUSTIBLE
	// =======================================================
	public static final String COMBUSTIBLE_ID = "id";
	public static final String COMBUSTIBLE_DENOMINACION = "denominacion";
	public static final String COMBUSTIBLE_CODIGO = "codigo";
	public static final String COMBUSTIBLE_EXT_CODE = "ext_code";

	// =======================================================
	// PRECIO_COMBUSTIBLE
	// =======================================================
	public static final String PRECIOCOMBUSTIBLE_ID_COMBUSTIBLE = "id_combustible";
	public static final String PRECIOCOMBUSTIBLE_ID_EESS = "id_eess";
	public static final String PRECIOCOMBUSTIBLE_FECHA = "fecha";
	public static final String PRECIOCOMBUSTIBLE_PRECIO = "precio";

	// =======================================================
	// PRECIO_COMBUSTIBLE
	// =======================================================
	public static final String COMBUSTIBLE_DISPONIBLE_ID_COMBUSTIBLE = "id_combustible";
	public static final String COMBUSTIBLE_DISPONIBLE_ID_EESS= "id_eess";
}
