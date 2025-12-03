package com.inggarciabaldo.carburo.apiReq;

import com.inggarciabaldo.carburo.util.network.HttpClient;
import com.inggarciabaldo.carburo.util.properties.PropertyLoader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase encargada de realizar peticiones HTTP hacia los endpoints del servicio de gasolineras.
 * Permite consultar datos actuales e históricos de estaciones, así como listados de entidades geográficas
 * (CCAA, provincias, municipios) y productos (combustibles).
 * <p>
 * Los endpoints se definen en el archivo: resources/endpoints.properties
 */
public class GasStationHttpRequest {

	// ============================
	// PLACEHOLDERS DE ENDPOINTS
	// ============================

	public static final String PARAM_FECHA = "FECHA";
	public static final String PARAM_IDCCAA = "IDCCAA";
	public static final String PARAM_IDPROVINCIA = "IDPROVINCIA";
	public static final String PARAM_IDMUNICIPIO = "IDMUNICIPIO";
	public static final String PARAM_IDPRODUCTO = "IDPRODUCTO";


	//Utilitarios
	private final HttpClient httpClient;
	private final PropertyLoader propertyLoader;

	public GasStationHttpRequest() {
		//Inicialización de los archivos de propiedades
		this.propertyLoader = PropertyLoader.getInstance();

		//Inicializamos el cliente Http
		try {
			//Inicializamos el cliente HTTP
			this.httpClient = new HttpClient();
		}
		//Comprobamos las excepciones del cliente HTTP
		catch (NoSuchAlgorithmException | KeyManagementException e) {
			throw new IllegalStateException("No se ha podido cargar el cliente HTTP", e);
		}
	}

	// ---------------------------------------------------------------------------------------------
	// MÉTODOS UTILITARIOS
	// ---------------------------------------------------------------------------------------------

	/**
	 * Construye la URL completa del endpoint, sustituyendo placeholders por los valores dados.
	 *
	 * @param key    La clave del endpoint en endpoints.properties
	 * @param params Los valores para sustituir en la ruta del endpoint
	 * @return La URL completa del endpoint
	 * @throws IllegalArgumentException Si alguno de los valores no es válido.
	 * @throws IllegalStateException    Si no se encuentran los valores o la URL base no está definida.
	 */
	private String buildUrl(String key, Map<String, Object> params)
			throws IllegalArgumentException, IllegalStateException {
		if (key == null || key.isBlank()) throw new IllegalArgumentException(
				"Argumento clave no válido para construir la URL: " + key);

		// Cargamos la url base
		String base = propertyLoader.getEndpointProperty("base.url");
		if (base == null) //Comprobamos su validez
			throw new IllegalStateException(
					"No se ha encontrado la URL base en endpoints.properties.");

		// Cargamos la ruta del endpoint definida por la clave
		String path = propertyLoader.getEndpointProperty(key);
		if (path == null) //Comprobamos su validez
			throw new IllegalArgumentException(
					"No se ha encontrado ningún endpoint con la clave: " + key);

		// Formateamos la ruta con los valores proporcionados
		if (params != null) {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				String placeholder = "{" + entry.getKey() + "}";
				if (!path.contains(placeholder)) {
					throw new IllegalArgumentException("El placeholder " + placeholder +
															   " no existe en el endpoint " +
															   key);
				}
				path = path.replace(placeholder, entry.getValue().toString());
			}
		}

		return base + path;
	}

	/**
	 * Realiza una petición GET al endpoint indicado y devuelve el resultado en formato JSON.
	 */
	private JSONObject getJson(String key, Map<String, Object> params)
			throws IOException {
		String url = buildUrl(key, params);
		return httpClient.obtenerJsonObjet(url);
	}

	private JSONArray getJsonArray(String key, Map<String, Object> params)
			throws IOException {
		String url = buildUrl(key, params);
		return httpClient.obtenerJsonArray(url);
	}

	// ---------------------------------------------------------------------------------------------
	// ESTACIONES TERRESTRES (ACTUALES)
	// ---------------------------------------------------------------------------------------------

	/**
	 * Obtiene todas las estaciones disponibles actualmente.
	 */
	public JSONObject getAllStations() throws IOException {
		return getJson("estaciones.terrestres", null);
	}

	// ============================
	// ESTACIONES TERRESTRES (ACTUALES)
	// ============================

	/**
	 * Filtra estaciones por Comunidad Autónoma
	 */
	public JSONObject getStationsByCCAA(int idCCAA) throws IOException {
		Map<String, Object> params = new HashMap<>();
		params.put(PARAM_IDCCAA, idCCAA);
		return getJson("estaciones.terrestres.filtroCCAA", params);
	}

	/**
	 * Filtra estaciones por provincia
	 */
	public JSONObject getStationsByProvince(int idProvincia) throws IOException {
		Map<String, Object> params = new HashMap<>();
		params.put(PARAM_IDPROVINCIA, idProvincia);
		return getJson("estaciones.terrestres.filtroProvincia", params);
	}

	/**
	 * Filtra estaciones por municipio
	 */
	public JSONObject getStationsByMunicipio(int idMunicipio) throws IOException {
		Map<String, Object> params = new HashMap<>();
		params.put(PARAM_IDMUNICIPIO, idMunicipio);
		return getJson("estaciones.terrestres.filtroMunicipio", params);
	}

	/**
	 * Filtra estaciones por tipo de combustible
	 */
	public JSONObject getStationsByProduct(int combustible) throws IOException {
		Map<String, Object> params = new HashMap<>();
		params.put(PARAM_IDPRODUCTO, combustible);
		return getJson("estaciones.terrestres.filtroProducto", params);
	}

	/**
	 * Filtra estaciones por provincia y producto
	 */
	public JSONObject getStationsByProvinceAndProduct(int idProvincia, int combustible)
			throws IOException {
		Map<String, Object> params = new HashMap<>();
		params.put(PARAM_IDPROVINCIA, idProvincia);
		params.put(PARAM_IDPRODUCTO, combustible);
		return getJson("estaciones.terrestres.filtroProvinciaProducto", params);
	}

	/**
	 * Filtra estaciones por CCAA y producto
	 */
	public JSONObject getStationsByCCAAAndProduct(int idCCAA, int combustible)
			throws IOException {
		Map<String, Object> params = new HashMap<>();
		params.put(PARAM_IDCCAA, idCCAA);
		params.put(PARAM_IDPRODUCTO, combustible);
		return getJson("estaciones.terrestres.filtroCCAAProducto", params);
	}

	/**
	 * Filtra estaciones por municipio y producto
	 */
	public JSONObject getStationsByMunicipioAndProduct(int idMunicipio, int combustible)
			throws IOException {
		Map<String, Object> params = new HashMap<>();
		params.put(PARAM_IDMUNICIPIO, idMunicipio);
		params.put(PARAM_IDPRODUCTO, combustible);
		return getJson("estaciones.terrestres.filtroMunicipioProducto", params);
	}

	// ============================
	// ESTACIONES TERRESTRES (HISTÓRICAS)
	// ============================

	/**
	 * Obtiene todas las estaciones registradas en una fecha histórica
	 */
	public JSONObject getAllStationsHist(LocalDate fecha) throws IOException {
		Map<String, Object> params = new HashMap<>();
		params.put(PARAM_FECHA, formatDate(fecha));
		return getJson("estaciones.terrestres.hist", params);
	}

	/**
	 * Filtra estaciones históricas por CCAA y fecha
	 */
	public JSONObject getStationsHistByCCAA(LocalDate fecha, int idCCAA)
			throws IOException {
		Map<String, Object> params = new HashMap<>();
		params.put(PARAM_FECHA, formatDate(fecha));
		params.put(PARAM_IDCCAA, idCCAA);
		return getJson("estaciones.terrestres.hist.filtroCCAA", params);
	}

	/**
	 * Filtra estaciones históricas por provincia y fecha
	 */
	public JSONObject getStationsHistByProvince(LocalDate fecha, int idProvincia)
			throws IOException {
		Map<String, Object> params = new HashMap<>();
		params.put(PARAM_FECHA, formatDate(fecha));
		params.put(PARAM_IDPROVINCIA, idProvincia);
		return getJson("estaciones.terrestres.hist.filtroProvincia", params);
	}

	/**
	 * Filtra estaciones históricas por municipio y fecha
	 */
	public JSONObject getStationsHistByMunicipio(LocalDate fecha, int idMunicipio)
			throws IOException {
		Map<String, Object> params = new HashMap<>();
		params.put(PARAM_FECHA, formatDate(fecha));
		params.put(PARAM_IDMUNICIPIO, idMunicipio);
		return getJson("estaciones.terrestres.hist.filtroMunicipio", params);
	}

	/**
	 * Filtra estaciones históricas por tipo de combustible y fecha
	 */
	public JSONObject getStationsHistByProduct(LocalDate fecha, int combustible)
			throws IOException {
		Map<String, Object> params = new HashMap<>();
		params.put(PARAM_FECHA, formatDate(fecha));
		params.put(PARAM_IDPRODUCTO, combustible);
		return getJson("estaciones.terrestres.hist.filtroProducto", params);
	}

	/**
	 * Filtra estaciones históricas por provincia, producto y fecha
	 */
	public JSONObject getStationsHistByProvinceAndProduct(LocalDate fecha,
														  int idProvincia,
														  int combustible)
			throws IOException {
		Map<String, Object> params = new HashMap<>();
		params.put(PARAM_FECHA, formatDate(fecha));
		params.put(PARAM_IDPROVINCIA, idProvincia);
		params.put(PARAM_IDPRODUCTO, combustible);
		return getJson("estaciones.terrestres.hist.filtroProvinciaProducto", params);
	}

	/**
	 * Filtra estaciones históricas por CCAA, producto y fecha
	 */
	public JSONObject getStationsHistByCCAAAndProduct(LocalDate fecha, int idCCAA,
													  int combustible)
			throws IOException {
		Map<String, Object> params = new HashMap<>();
		params.put(PARAM_FECHA, formatDate(fecha));
		params.put(PARAM_IDCCAA, idCCAA);
		params.put(PARAM_IDPRODUCTO, combustible);
		return getJson("estaciones.terrestres.hist.filtroCCAAProducto", params);
	}

	/**
	 * Filtra estaciones históricas por municipio, producto y fecha
	 */
	public JSONObject getStationsHistByMunicipioAndProduct(LocalDate fecha,
														   int idMunicipio,
														   int combustible)
			throws IOException {
		Map<String, Object> params = new HashMap<>();
		params.put(PARAM_FECHA, formatDate(fecha));
		params.put(PARAM_IDMUNICIPIO, idMunicipio);
		params.put(PARAM_IDPRODUCTO, combustible);
		return getJson("estaciones.terrestres.hist.filtroMunicipioProducto", params);
	}

	/**
	 * Formatea la fecha según el patrón definido en endpoints.properties
	 */
	private String formatDate(LocalDate fecha) {
		return fecha.format(DateTimeFormatter.ofPattern(
				propertyLoader.getEndpointProperty(
						"estaciones.terrestres.hist.pattern")));
	}

	// ============================
	// LISTADOS
	// ============================

	/**
	 * Obtiene el listado completo de Comunidades Autónomas.
	 */
	public JSONObject getCCAAList() throws IOException {
		return getJson("listados.ccaa", null);
	}

	/**
	 * Obtiene el listado completo de Provincias.
	 */
	public JSONObject getProvinceList() throws IOException {
		return getJson("listados.provincias", null);
	}

	/**
	 * Obtiene el listado de Provincias pertenecientes a una Comunidad Autónoma específica.
	 */
	public JSONObject getProvinceListByCCAA(int idCCAA) throws IOException {
		Map<String, Object> params = new HashMap<>();
		params.put(PARAM_IDCCAA, idCCAA);
		return getJson("listados.provinciasPorCCAA", params);
	}

	/**
	 * Obtiene el listado completo de Municipios.
	 */
	public JSONArray getMunicipioList() throws IOException {
		return getJsonArray("listados.municipios", null);
	}

	/**
	 * Obtiene el listado de Municipios pertenecientes a una Provincia específica.
	 */
	public JSONObject getMunicipioListByProvince(int idProvincia) throws IOException {
		Map<String, Object> params = new HashMap<>();
		params.put(PARAM_IDPROVINCIA, idProvincia);
		return getJson("listados.municipiosPorProvincia", params);
	}

	/**
	 * Obtiene el listado completo de productos petrolíferos.
	 */
	public JSONObject getProductList() throws IOException {
		return getJson("listados.productos", null);
	}


}
