package com.inggarciabaldo.carburo.util.network;

import com.inggarciabaldo.carburo.util.log.Loggers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;

/**
 * Clase HttpClient
 * ----------------
 * Cliente HTTP basado en OkHttp para realizar peticiones GET y obtener respuestas JSON.
 *
 * <p>Esta implementación sustituye el uso de {@link java.net.HttpURLConnection},
 * aprovechando las ventajas de OkHttp: soporte para HTTP/2, mejor gestión de errores,
 * conexiones persistentes y mayor eficiencia.</p>
 *
 * <p>Se utiliza en la aplicación para realizar llamadas a servicios REST externos
 * (por ejemplo, los endpoints del Ministerio para la Transición Ecológica).</p>
 */
public class HttpClient {

	private static final Logger logger = Loggers.GENERAL;

	// ---------------------------------------------------------------------------------------------
	// Constantes
	// ---------------------------------------------------------------------------------------------

	/**
	 * Tiempo máximo de espera para conexión y lectura (en segundos).
	 */
	private static final int TIEMPO_ESPERA_SEGUNDOS = 20;

	/**
	 * Cabecera estándar utilizada en todas las peticiones HTTP.
	 */
	private static final String CABECERA_ACCEPT = "Accept";

	/**
	 * Valor de la cabecera Accept para indicar que se espera una respuesta JSON.
	 */
	private static final String TIPO_JSON = "application/json";

	// ---------------------------------------------------------------------------------------------
	// Atributos
	// ---------------------------------------------------------------------------------------------

	/**
	 * Cliente HTTP reutilizable.
	 */
	private final OkHttpClient clienteHttp;

	// ---------------------------------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------------------------------

	/**
	 * Inicializa el cliente OkHttp con los tiempos de espera configurados.
	 */
	public HttpClient() throws NoSuchAlgorithmException, KeyManagementException {
		this.clienteHttp = new OkHttpClient.Builder().connectTimeout(
						Duration.ofSeconds(TIEMPO_ESPERA_SEGUNDOS))
				.readTimeout(Duration.ofSeconds(TIEMPO_ESPERA_SEGUNDOS))
				.hostnameVerifier((hostname, session) -> true).build();

//		TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() { TODO borrar si ya no falla.
//			public void checkClientTrusted(X509Certificate[] chain, String authType) {
//			}
//
//			public void checkServerTrusted(X509Certificate[] chain, String authType) {
//			}
//
//			public X509Certificate[] getAcceptedIssuers() {
//				return new X509Certificate[0];
//			}
//		}};
//
//		SSLContext sslContext = SSLContext.getInstance("TLS");
//		sslContext.init(null, trustAllCerts, new SecureRandom());
//		this.clienteHttp = new OkHttpClient.Builder().sslSocketFactory(
//						sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
//				.hostnameVerifier((hostname, session) -> true).build().newBuilder()
//				.build();
	}

	// ---------------------------------------------------------------------------------------------
	// Métodos públicos
	// ---------------------------------------------------------------------------------------------

	/**
	 * Realiza una petición GET a la URL indicada y devuelve la respuesta como objeto JSON.
	 *
	 * @param urlString URL completa del recurso a consultar.
	 * @return {@link JSONObject} con el contenido de la respuesta.
	 * @throws IOException Si ocurre un error de conexión o lectura.
	 */
	public JSONObject obtenerJsonObjet(String urlString) throws IOException {
		String cuerpoRespuesta = ejecutarPeticion(urlString);
		return new JSONObject(cuerpoRespuesta);
	}

	/**
	 * Realiza una petición GET a la URL indicada y devuelve la respuesta como array JSON.
	 *
	 * @param urlString URL completa del recurso a consultar.
	 * @return {@link JSONArray} con el contenido de la respuesta.
	 * @throws IOException Si ocurre un error de conexión o lectura.
	 */
	public JSONArray obtenerJsonArray(String urlString) throws IOException {
		String cuerpoRespuesta = ejecutarPeticion(urlString);
		return new JSONArray(cuerpoRespuesta);
	}

	// ---------------------------------------------------------------------------------------------
	// Métodos privados auxiliares
	// ---------------------------------------------------------------------------------------------

	/**
	 * Ejecuta una petición HTTP GET y devuelve el cuerpo de la respuesta como String.
	 *
	 * @param urlString URL completa del recurso a consultar.
	 * @return Contenido del cuerpo de la respuesta.
	 * @throws IOException Si ocurre un error de conexión, lectura o respuesta vacía.
	 */
	private String ejecutarPeticion(String urlString) throws IOException {
		Request request = construirPeticion(urlString);

		try (Response response = clienteHttp.newCall(request).execute()) {

			if (!response.isSuccessful()) {
				logger.error("Error HTTP {} al acceder a {}", response.code(), urlString);
				throw new IOException(
						"Error HTTP " + response.code() + " al acceder a: " + urlString);
			}

			ResponseBody body = response.body();
			if (body == null) {
				logger.error("Respuesta vacía al acceder a {}", urlString);
				throw new IOException("Respuesta HTTP vacía al acceder a: " + urlString);
			}

			return body.string();
		} catch (IOException e) {
			logger.error("Error ejecutando petición GET a {}: {}", urlString,
						 e.getMessage());
			throw e;
		}
	}

	/**
	 * Construye una petición GET estándar con cabecera JSON.
	 */
	private Request construirPeticion(String urlString) {
		return new Request.Builder().url(urlString).get()
				.addHeader(CABECERA_ACCEPT, TIPO_JSON).build();
	}
}
