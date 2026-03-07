package app.carburo.loader.util.network;

import app.carburo.loader.util.log.Loggers;
import app.carburo.loader.util.properties.PropertyLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.time.Duration;

/**
 * Clase OwnHttpClient
 * ----------------
 * Cliente HTTP basado en {@link HttpClient} para realizar peticiones GET y obtener respuestas JSON.
 *
 * <p>Se utiliza en la aplicación para realizar llamadas a servicios REST externos
 * (por ejemplo, los endpoints del Ministerio para la Transición Ecológica).</p>
 */
public class OwnHttpClient {

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
	private final HttpClient clienteHttp;

	// ---------------------------------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------------------------------

	/**
	 * Inicializa el cliente Http con los tiempos de espera configurados y la configuración de ssl definida.
	 */
	public OwnHttpClient() {
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
		HttpClient.Builder builder = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(tiempoEsperaSegundos));

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

				builder.sslContext(sslContext);

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
		try {
			HttpRequest request = construirPeticion(urlString);
			HttpResponse<String> response = clienteHttp.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() / 100 != 2) {
				logger.error("Error HTTP {} al acceder a {}", response.statusCode(), urlString);
				throw new IOException(
						"Error HTTP " + response.statusCode() + " al acceder a: " + urlString);
			}

			String body = response.body();
			if (body == null) {
				logger.error("Respuesta vacía al acceder a {}", urlString);
				throw new IOException("Respuesta HTTP vacía al acceder a: " + urlString);
			}

			return body;
		} catch (IOException e) {
			logger.error("Error ejecutando petición GET a {}: {}", urlString,
						 e.getMessage());
			throw e;
		} catch (InterruptedException e) {
			logger.error("Error de interrupción en la ejecución de la petición petición GET a {}: {}", urlString,
					e.getMessage());
			throw new IOException(e);
		}
	}

	/**
	 * Construye una petición GET estándar con cabecera JSON.
	 */
	private HttpRequest construirPeticion(String urlString) {
		return HttpRequest.newBuilder().uri(URI.create(urlString)).GET().header(CABECERA_ACCEPT, TIPO_JSON)
				.timeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SEGUNDOS)).build();
	}
}
