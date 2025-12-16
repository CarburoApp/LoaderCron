package com.inggarciabaldo.carburo.application.service.util.crud.command;

import com.inggarciabaldo.carburo.application.Factorias;
import com.inggarciabaldo.carburo.application.model.ComunidadAutonoma;
import com.inggarciabaldo.carburo.application.model.Municipio;
import com.inggarciabaldo.carburo.application.model.Provincia;
import com.inggarciabaldo.carburo.config.cache.ApplicationCache;

import java.util.Map;
import java.util.Optional;

/**
 * Clase auxiliar que centraliza los métodos auxiliares necesitados por los EntityAssembler.
 */
public class EntityAssemblerAuxiliar {

	// Constructor privado para evitar instancias innecesarias.
	private EntityAssemblerAuxiliar(){}


	/**
	 * Devuelve una {@link Provincia} a partir de su identificador externo.
	 *
	 * <p>
	 * El método intenta primero recuperar la provincia desde la caché de la
	 * aplicación. Si no se encuentra en caché, se consulta la base de datos
	 * mediante el servicio correspondiente.
	 * </p>
	 *
	 * <p>
	 * Se utiliza este método en lugar de acceder directamente al servicio
	 * para evitar la creación de hilos de acceso innecesarios desde los
	 * EntityAssembler.
	 * </p>
	 *
	 * @param id {@link Short} identificador externo de la provincia
	 * @return la {@link Provincia} asociada a ese identificador
	 * @throws IllegalStateException si no se encuentra la provincia
	 */
	public static Provincia getProvincia(short id) {
		// Comrpuebo si está en caché
		Map<Short, Provincia> cache = ApplicationCache.instance.getProvincias();
		if (cache != null && !cache.isEmpty()) {
			for (Provincia provincia : cache.values())
				if (provincia.getExtCode() == id) return provincia;
		}

		Optional<Provincia> provincia = Factorias.service.forProvincia()
				.findProvinciaById(id);
		if (provincia.isEmpty()) throw new IllegalStateException(
				"No se ha encontrado la provincia. Id(Provincia): " + id);
		return provincia.get();
	}

	/**
	 * Devuelve una {@link ComunidadAutonoma} a partir de su identificador externo.
	 *
	 * <p>
	 * El método busca primero la comunidad autónoma en la caché de la aplicación.
	 * En caso de no encontrarla, realiza la consulta a la base de datos a través
	 * del servicio correspondiente.
	 * </p>
	 *
	 * <p>
	 * Este enfoque permite reducir accesos innecesarios a la BD y desacoplar
	 * los EntityAssembler de los servicios de dominio.
	 * </p>
	 *
	 * @param id {@link Short} identificador externo de la comunidad autónoma
	 * @return la {@link ComunidadAutonoma} asociada a ese identificador
	 * @throws IllegalStateException si no se encuentra la comunidad autónoma
	 */
	public static ComunidadAutonoma getComunidadAutonoma(short id) {
		// Comrpuebo si está en caché
		Map<Short, ComunidadAutonoma> cache = ApplicationCache.instance.getComunidadesAutonomas();
		if (cache != null && !cache.isEmpty()) {
			for (ComunidadAutonoma ccaa : cache.values())
				if (ccaa.getExtCode() == id) return ccaa;
		}

		Optional<ComunidadAutonoma> ca = Factorias.service.forCCAAService()
				.findComunidadAutonomaById(id);
		if (ca.isEmpty()) throw new IllegalStateException(
				"No se ha encontrado la Comunidad Autónoma. Id(CCAA): " + id);
		return ca.get();
	}

	/**
	 * Devuelve un {@link Municipio} a partir de su identificador externo.
	 *
	 * <p>
	 * El método prioriza la obtención del municipio desde la caché de la
	 * aplicación. Si el municipio no se encuentra almacenado en caché,
	 * se realiza la consulta a la base de datos mediante el servicio correspondiente.
	 * </p>
	 *
	 * <p>
	 * Esta lógica evita accesos repetidos a la BD y mantiene una única
	 * estrategia de recuperación de entidades dentro de los EntityAssembler.
	 * </p>
	 *
	 * @param id {@link Short} identificador externo del municipio
	 * @return el {@link Municipio} asociado a ese identificador
	 * @throws IllegalStateException si no se encuentra el municipio
	 */
	public static Municipio getMunicipio(short id) {
		// Comrpuebo si está en caché
		Map<Short, Municipio> cache = ApplicationCache.instance.getMunicipios();
		if (cache != null && !cache.isEmpty()) {
			for (Municipio item : cache.values())
				if (item.getExtCode() == id) return item;
		}

		Optional<Municipio> municipio = Factorias.service.forMunicipio()
				.findMunicipioById(id);
		if (municipio.isEmpty()) throw new IllegalStateException(
				"No se ha encontrado el municipio. Id(Municipio): " + id);
		return municipio.get();
	}
}
