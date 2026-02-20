package app.carburo.loader.util.network;

import app.carburo.loader.util.log.Loggers;
import app.carburo.loader.util.properties.PropertyLoader;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
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
	private static final int DEFAULT_TIMEOUT_SEGUNDOS = 20;

	/**
	 * Nombre de la propiedad para configurar timeout HTTP en segundos
	 */
	private static final String PROP_TIMEOUT_SEGUNDOS = "httpCliente.request.timeOutSegundos";

	/**
	 * Cabecera estándar utilizada en todas las peticiones HTTP.
	 */
	private static final String CABECERA_ACCEPT = "Accept";

	/**
	 * Valor de la cabecera Accept para indicar que se espera una respuesta JSON.
	 */
	private static final String TIPO_JSON = "application/json";

	// Datos del certificado ssl
	private static final String PROP_CERT_ENABLED = "ssl.cert.enabled";
	private static final String PROP_CERT_FILE = "ssl.cert.file";
	private static final String PROP_CERT_TYPE = "ssl.cert.type";
	private static final String DEFAULT_CERT_TYPE = "X.509";

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
	public HttpClient() {
		// Leemos el timeout desde properties, usando valor por defecto si no existe
		int tiempoEsperaSegundos = Integer.parseInt(PropertyLoader.getInstance()
															.getApplicationProperty(
																	PROP_TIMEOUT_SEGUNDOS,
																	String.valueOf(
																			DEFAULT_TIMEOUT_SEGUNDOS)));
		boolean sslEnabled = Boolean.parseBoolean(
				PropertyLoader.getInstance()
						.getApplicationProperty(PROP_CERT_ENABLED, "false")
		);
		OkHttpClient.Builder builder = new OkHttpClient.Builder()
				.connectTimeout(Duration.ofSeconds(tiempoEsperaSegundos))
				.readTimeout(Duration.ofSeconds(tiempoEsperaSegundos));

		if (sslEnabled) {
			String certFileName = PropertyLoader.getInstance()
					.getApplicationProperty(PROP_CERT_FILE);
			String certType = PropertyLoader.getInstance()
					.getApplicationProperty(PROP_CERT_TYPE, DEFAULT_CERT_TYPE);

			Path certPath = Paths.get(System.getProperty("user.dir"), certFileName);

			if (!Files.exists(certPath))
				throw new RuntimeException("No se encontró el certificado en: " + certPath);
			try (InputStream is = Files.newInputStream(certPath)) {
				CertificateFactory cf = CertificateFactory.getInstance(certType);
				Certificate cert = cf.generateCertificate(is);

				KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
				keyStore.load(null, null);
				keyStore.setCertificateEntry("mitma-cert", cert);

				TrustManagerFactory tmf = TrustManagerFactory.getInstance(
						TrustManagerFactory.getDefaultAlgorithm());
				tmf.init(keyStore);

				SSLContext sslContext = SSLContext.getInstance("TLS");
				sslContext.init(null, tmf.getTrustManagers(), null);

				builder.sslSocketFactory(
						sslContext.getSocketFactory(),
						(X509TrustManager) tmf.getTrustManagers()[0]
				);

				logger.info("SSL configurado usando certificado externo: {}", certPath);
			} catch (Exception e) {
				throw new RuntimeException("Error configurando SSL con certificado externo", e);
			}
		}
		this.clienteHttp = builder.build();
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
