package com.inggarciabaldo.carburo.config.cache;

import com.inggarciabaldo.carburo.application.model.Combustible;
import com.inggarciabaldo.carburo.application.model.ComunidadAutonoma;
import com.inggarciabaldo.carburo.application.model.Municipio;
import com.inggarciabaldo.carburo.application.model.Provincia;
import com.inggarciabaldo.carburo.util.log.Loggers;
import org.slf4j.Logger;

import java.util.*;

/**
 * En este sistema, el acceso lógico a los datos se realiza de manera directa a la base de datos a traves de JDBC.
 * Esto presenta una serie de ventajas y desventajas. En primer lugar es más sencillo de
 * implementar y mantener, siendo más eficiente en instancias que actúen sobre datos normalmente distintos.
 * <p>
 * Sin embargo, en esta aplicación hay un gran problema de eficiencia, que es el acceso
 * repetido miles de veces a una serie de datos que llegarían a permanecer invariables durante prácticamente
 * todo el ciclo de vida de la aplicación. Por ello se ha decidido implementar una caché de aplicación.
 * <p>
 * Esta clase es un simple almacenamiento de datos prácticamente invariables y muy usados, tanto que podrían
 * haberse implementado como Enumerados, pero por otros motivos se hicieron como entidades simples.
 * Estos datos son:
 * - Comunidades Autónomas
 * - Provincias
 * - Municipios
 * - Tipos de Combustible
 * <p>
 * La caché se carga en su primer uso y se mantiene en memoria durante todo su ciclo de vida.
 * <p>
 * La caché será gestionada únicamente por los commands de la capa de servicio de la aplicación.
 */
public class ApplicationCache {

	private static final String LOG_ETIQUETA_CACHE = "[CACHÉ DE LA APLICACIÓN] ";
	private final Logger logger = Loggers.DB;

	// Patrón Singleton para evitar múltiples instancias que consuman memoria innecesariamente
	public static ApplicationCache instance = new ApplicationCache();

	private ApplicationCache() {} // Constructor privado para evitar instanciación externa

	/**
	 * Datos a definir como caché de aplicación:
	 * - Tipos de Combustible :: Definido como un Set para evitar duplicados y permitir búsquedas rápidas.
	 * - Comunidades Autónomas :: Definido como un Map para permitir búsquedas rápidas por ID.
	 * - Provincias :: Definido como un Map para permitir búsquedas rápidas por ID.
	 * - Municipios :: Definido como un Map para permitir búsquedas rápidas por ID.
	 */
	private final Set<Combustible> _tiposDeCombustible = new HashSet<>();
	private final Map<Short, ComunidadAutonoma> _comunidadesAutonomas = new HashMap<>();
	private final Map<Short, Provincia> _provincias = new HashMap<>();
	private final Map<Short, Municipio> _municipios = new HashMap<>();

	/**
	 * Limpia la caché de la aplicación, eliminando todos los datos almacenados.
	 */
	public void clearCache() {
		this._tiposDeCombustible.clear();
		this._comunidadesAutonomas.clear();
		this._provincias.clear();
		this._municipios.clear();
		logger.info(LOG_ETIQUETA_CACHE + "Se ha limpiado la caché.");
	}

	/*
	 * Tipos de Combustble
	 */

	/**
	 * Devuelve una copia inmutable del Set de tipos de combustible almacenados en caché.
	 *
	 * @return Set inmutable de tipos de combustible.
	 */
	public Set<Combustible> getTiposDeCombustible() {
		return Set.copyOf(_tiposDeCombustible);
	}

	/**
	 * Añade uno o más tipos de combustible a la caché.
	 *
	 * @param combustibles tipos de combustible a introducir en la caché.
	 */
	public void addCombustibles(List<Combustible> combustibles) {
		for (Combustible combustible : combustibles)
			if (combustible == null) throw new IllegalArgumentException(
					"No se pueden añadir Combustibles nulas a la caché.");
			else _tiposDeCombustible.add(combustible);
		logger.info(LOG_ETIQUETA_CACHE +
							"Se han añadido a la caché {} tipos de Combustibles.",
					combustibles.size());
	}

	/*
	 * Comunidades Autónomas
	 */

	/**
	 * Devuelve una copia inmutable del Map de Comunidades Autónomas almacenadas en caché.
	 *
	 * @return Map inmutable de Comunidades Autónomas.
	 */
	public Map<Short, ComunidadAutonoma> getComunidadesAutonomas() {
		return Map.copyOf(_comunidadesAutonomas);
	}

	/**
	 * Devuelve una Comunidad Autónoma por su ID.
	 *
	 * @param id @{@link Short} entero positivo que representa el ID de la Comunidad Autónoma.
	 * @return Comunidad Autónoma correspondiente al ID proporcionado o null si no existe.
	 */
	public ComunidadAutonoma getComunidadAutonomaById(short id) {
		return _comunidadesAutonomas.get(id);
	}

	/**
	 * Añade uno o más Comunidades Autónomas a la caché.
	 *
	 * @param ccaas comunidades autónomas a introducir en la caché.
	 */
	public void addCCAAs(List<ComunidadAutonoma> ccaas) {
		for (ComunidadAutonoma comunidadAutonoma : ccaas)
			if (comunidadAutonoma == null) throw new IllegalArgumentException(
					"No se pueden añadir Comunidades Autónomas nulas a la caché.");
			else _comunidadesAutonomas.put(comunidadAutonoma.getId(), comunidadAutonoma);
		logger.info(LOG_ETIQUETA_CACHE +
							"Se han añadido a la caché {} Comunidades Autónomas.",
					ccaas.size());
	}

	/*
	 * Provincias
	 */

	/**
	 * Devuelve una copia inmutable del Map de Provincias almacenadas en caché.
	 *
	 * @return Map inmutable de Provincias.
	 */
	public Map<Short, Provincia> getProvincias() {
		return Map.copyOf(_provincias);
	}

	/**
	 * Devuelve una Provincia por su ID.
	 *
	 * @param id @{@link Short} entero positivo que representa el ID de la Provincia.
	 * @return Provincia correspondiente al ID proporcionado o null si no existe.
	 */
	public Provincia getProvinciaById(short id) {
		return _provincias.get(id);
	}

	/**
	 * Añade una o más provincias a la caché.
	 *
	 * @param provincias provincias a introducir en la caché.
	 */
	public void addProvincias(List<Provincia> provincias) {
		for (Provincia provincia : provincias)
			if (provincia == null) throw new IllegalArgumentException(
					"No se pueden añadir Provincias nulas a la caché.");
			else _provincias.put(provincia.getId(), provincia);
		logger.info(LOG_ETIQUETA_CACHE + "Se han añadido a la caché {} Provincias.",
					provincias.size());
	}

	/*
	 * Municipios
	 */

	/**
	 * Devuelve una copia inmutable del Map de Municipios almacenados en caché.
	 *
	 * @return Map inmutable de Municipios.
	 */
	public Map<Short, Municipio> getMunicipios() {
		return Map.copyOf(_municipios);
	}

	/**
	 * Devuelve un Municipio por su ID.
	 *
	 * @param id @{@link Short} entero positivo que representa el ID del Municipio.
	 * @return @{@link Municipio} correspondiente al ID proporcionado o null si no existe.
	 */
	public Municipio getMunicipioById(short id) {
		return _municipios.get(id);
	}

	/**
	 * Añade uno o más municipios a la caché.
	 *
	 * @param municipios municipios a introducir en la caché.
	 */
	public void addMuncipios(List<Municipio> municipios) {
		for (Municipio m : municipios)
			if (m == null) throw new IllegalArgumentException(
					"No se pueden añadir Municipios nulos a la caché.");
			else _municipios.put(m.getId(), m);
		logger.info(LOG_ETIQUETA_CACHE + "Se han añadido a la caché {} Municipios.",
					municipios.size());
	}
}
