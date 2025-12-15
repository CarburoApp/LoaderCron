package com.inggarciabaldo.carburo.scheduler.jobs;

import com.inggarciabaldo.carburo.application.Factorias;
import com.inggarciabaldo.carburo.application.model.Combustible;
import com.inggarciabaldo.carburo.application.model.EstacionDeServicio;
import com.inggarciabaldo.carburo.application.model.PrecioCombustible;
import com.inggarciabaldo.carburo.application.service.eess.EESSCrudService;
import com.inggarciabaldo.carburo.application.service.util.dto.CombustibleDisponibleDTO;
import com.inggarciabaldo.carburo.util.log.Loggers;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * El objetivo de esta clase es recibir una colección de estaciones de servicio ya parseadas,
 * con sus respectivos precios y disponibilidades, y realizar el proceso de comparación para
 * persistirlas en la base de datos.
 */
public class ProcesadoDePersistenciaEESSaBD {

	private static final Logger loggerCron = Loggers.CRON;

	// Servicio de persistencia para eess
	private final EESSCrudService servicioEESS;
	private final DatoDeEjecucion datoDeEjecucion;
	private final Collection<EstacionDeServicio> estacionDeServicioParseada;

	public ProcesadoDePersistenciaEESSaBD(
			Collection<EstacionDeServicio> estacionDeServicioParseada,
			DatoDeEjecucion datoDeEjecucion) {
		// Comprobaciones
		if (datoDeEjecucion == null) throw new IllegalArgumentException(
				"No se puede procesar la persistencia de eess ya que el objeto de datos de ejecución es nulo.");
		if (estacionDeServicioParseada == null) throw new IllegalArgumentException(
				"No se puede procesar la persistencia de eess ya que la colección introducida es NULA.");

		if (estacionDeServicioParseada.isEmpty()) {
			loggerCron.warn("COLECCIÓN de EESS recibida para persistir está VACÍA.");

		}

		// Objeto de estadísticas.
		this.datoDeEjecucion = datoDeEjecucion;

		// Ordenamos las estaciones por su extCode
		this.estacionDeServicioParseada = estacionDeServicioParseada.stream()
				.sorted(Comparator.comparing(EstacionDeServicio::getExtCode)).toList();

		// Inicializo el servicio de persistencia
		this.servicioEESS = Factorias.service.forEESS();
	}


	/**
	 * Este metodo se encargará de comparar aquellas estaciones de servicio parseadas,
	 * actualmente introducidas por parámetro con las ya existentes en la base de datos.
	 * Si no existe la estación de servicio, se persistirá. Si ya existe, se comprobará si
	 * alguno de sus datos ha cambiado y en caso afirmativo se actualizará.
	 * <p>
	 * A parte, comprobará los precios de cada estación de servicio y, si no existen en la base de datos, los persistirá. En caso de que existan, comprobará si han cambiado y los actualizará en caso afirmativo.
	 * Hará lo correspondiente con la disponibilidad de cada estación de servicio.
	 */
	public void procesar() {
		loggerCron.info(
				"INICIO del PROCESAMIENTO de los objetos ya parseados para PERSISTIRLOS si fuese oportuno.");
		// Entrada prevista - Datos de la BD
		Collection<EstacionDeServicio> eessObtenidasDeLaBD;

		// Salidas previstas
		Collection<EstacionDeServicio> coleccionEESSFueraDeBD;
		Collection<EstacionDeServicio> coleccionEESSPresentesEnBD;
		Collection<EstacionDeServicio> coleccionEESSaActualizar;
		// Uso record xq solo necesito las ids, ahorro de memoria
		Collection<CombustibleDisponibleDTO> collectionDisponibilidad;
		Collection<PrecioCombustible> collectionPreciosAInsertar;
		Collection<PrecioCombustible> collectionPreciosAActualizar;

		/* Separamos en 3 bloques: EESS, Precios, Disponibilidad
		 * 1. EESS - Definimos si la estación ya está o no en la BD
		 * 		- Cogemos toda la información de la BD
		 * 		- Si no está la insertamos (SOLO LA EESS)
		 * 		- Si está, comprobamos si ha cambiado algún dato
		 * -> Salida del bloque
		 * 		coleccionEESSFueraDeBD (en bloque) -- no están y las insertamos en bloque sin disp ni precios
		 * 		coleccionEESSEnBD
		 * 		coleccionEESSaActualizar (en bloque) -- están pero han cambiado datos, de una en una
		 *
		 *
		 * 2. Disponibilidad - Definimos si la disponibilidad ya está o no en la BD
		 * 		- Aquella disponibilidad de eess que no esté en la BD la insertamos a la par
		 * 		- Despues de la inserción de eeess Cogemos toda la información de la BD
		 * 		- Si no está la insertamos
		 *  -> Salida del bloque collectionDisponibilidad a insertar sin (en bloque)
		 *
		 * 3. Precios - Definimos si el precio ya está o no en la BD
		 * 		- Aquellos precios de eess que no esté en la BD la insertamos a la par
		 * 		- Despues de la inserción de eeess Cogemos todos los precios de la para el día actual
		 * 		- Comprobamos si el precio (de entre los no insertados con eess) ya está establecido
		 * 		- Si no está lo insertamos
		 * 		- Si está comprobamos si ha cambiado
		 *
		 *  -> Salida del bloque collectionPreciosaInsertar a insertar (en bloque)
		 *  -> Salida del bloque collectionPreciosaActualizar a actualizar de uno en uno.
		 */

		// 1. Obtenemos las estaciones de servicio actualmente en la base de datos
		loggerCron.info("CARGAMOS las EESS de la BD.");
		eessObtenidasDeLaBD = getEstacionesDeServicioEnBD();

		// 2. Creo un HashMap y un set para optimizar la búsqueda con las ids que están en BD
		Map<Integer, EstacionDeServicio> eessEnBDPorExtCode = eessObtenidasDeLaBD.stream()
				.collect(Collectors.toMap(EstacionDeServicio::getExtCode, eess -> eess));
		Set<Integer> eessEnBDcomoExtCode = eessEnBDPorExtCode.keySet();


		// 3. Separo las estaciones de servicio parseadas que no se encuentran en BD y las que no
		// Las filtro por extCode, si está en el set o no, y las ordeno por extCode

		Map<Boolean, List<EstacionDeServicio>> particion = estacionDeServicioParseada.stream()
				.collect(Collectors.partitioningBy(
						eess -> eessEnBDcomoExtCode.contains(eess.getExtCode())));

		Comparator<EstacionDeServicio> byExtCode = Comparator.comparing(
				EstacionDeServicio::getExtCode);

		coleccionEESSPresentesEnBD = particion.get(true).stream().sorted(byExtCode)
				.toList();
		coleccionEESSFueraDeBD     = particion.get(false).stream().sorted(byExtCode)
				.toList();

		datoDeEjecucion.setTotalEESSParseadasFueraDeBD(coleccionEESSFueraDeBD.size());
		datoDeEjecucion.setTotalEESSParseadasEnBD(coleccionEESSPresentesEnBD.size());

		loggerCron.info(
				"Separación de las EESS parseadas en función de si EXISTE en la BD: Sí - {} EESS; No - {} EESS.",
				datoDeEjecucion.getTotalEESSParseadasEnBD(),
				datoDeEjecucion.getTotalEESSParseadasFueraDeBD());

		if (!coleccionEESSPresentesEnBD.isEmpty()) {
			//Asigna a cada eess de coleccionEESSEnBD el id que corresponde en BD.
			//Se asume que todas las estaciones de coleccionEESSEnBD existen en el Map.
			coleccionEESSPresentesEnBD.forEach(eessParseada -> {
				// Se asume que siempre existe
				eessParseada.setId(
						eessEnBDPorExtCode.get(eessParseada.getExtCode()).getId());
			});


			// 4. Definimos las estaciones de servicio a actualizar de aquellas estaciones de
			// servicio que se encuentran en BD
			coleccionEESSaActualizar = doComprobarEESSaActualizar(
					coleccionEESSPresentesEnBD, eessEnBDPorExtCode);

			// 5. Actualizamos aquellas estaciones que lo requiran
			doUpdateEESS(coleccionEESSaActualizar);
		} else loggerCron.info(
				"COLECCIÓN de EESS parseadas que ya se encontraría en BD está VACÍA. No se requiere actualización de ninguna de estas EESS.");


		if (!coleccionEESSFueraDeBD.isEmpty())
			// 6. Insertamos todas las estaciones de servicio que no están en BD
			doInsertEESSDisponibilidadYPreciosFueraDeBD(coleccionEESSFueraDeBD);
		else loggerCron.info(
				"COLECCIÓN de EESS parseadas que no se encontraría en BD está VACÍA. No se requiere persistir ninguna EESS.");

		// RESUMEN SITUACION ACTUAL:
		// - Se han persistido ya las eess, disponibilidad y precio de las eess de coleccionEESSEnBD
		// - Se han actualizado ya las eess que requerían actualización de coleccionEESSaActualizar
		// - Queda insertar/actualizar los precios y disponibilidades de las eess de coleccionEESSEnBD

		if (!coleccionEESSPresentesEnBD.isEmpty()) {
			try {
				// 7. Definimos la disponibilidad de las eess de entre las que están en BD.
				collectionDisponibilidad = getDisponibilidadAInsertar(
						coleccionEESSPresentesEnBD);

				// 8. Persistimos la disponibilidad obtenida
				if (!collectionDisponibilidad.isEmpty()) doInsertDisponibilidadCombustibles(
						collectionDisponibilidad);
				else loggerCron.info(
						"COLECCIÓN de combustibles disponibles a introducir está VACÍA. No se requiere persistir la disponibilidad de ningún combustible.");

			} catch (Exception e) {
				IllegalStateException exp = new IllegalStateException(
						"Error inesperado al intentar definir y persistir la disponibilidad de los combustibles de las EESS que ya están en BD. Por tanto no se ha podido completar el proceso de procesamiento.",
						e);
				loggerCron.error(
						"Error en procesamiento y persistencia de combustibles disponibles de eess que ya se encuentran en la BD. {}",
						e.getMessage(), e);
				throw exp;
			}

			// 9. Definimos los precios a insertar de las eess de entre las que están en BD
			collectionPreciosAInsertar = getPrecioCombustibleAInsertar(
					coleccionEESSPresentesEnBD);

			// 10. Persistimos los precios obtenidos
			if (!collectionPreciosAInsertar.isEmpty()) doInsertPrecioCombustibles(
					collectionPreciosAInsertar);
			else loggerCron.info(
					"COLECCIÓN de precios de combustibles de EESS que ya se encontraban en la BD a INSERTAR está VACÍA. No se requiere persistir ningún precio.");

			// 11. Definimos los precios a actualizar de las eess de entre las que están en BD
			collectionPreciosAActualizar = getPrecioCombustibleAActualizar(
					coleccionEESSPresentesEnBD);

			// 12. Actualizamos los precios obtenidos
			if (!collectionPreciosAActualizar.isEmpty()) doUpdatePrecioCombustibles(
					collectionPreciosAActualizar);
			else loggerCron.info(
					"COLECCIÓN de precios de combustibles de EESS que ya se encontraban en la BD que requieren de ACTUALIZACIÓN está VACÍA. No se requiere actualizar ningún precio.");

		} else loggerCron.info(
				"COLECCIÓN de EESS parseadas que ya se encontraría en BD está VACÍA. No se requieren acciones sobre la disponibilidad o precios de combustibles de estas EESS.");

	}


	/**
	 * Obtiene todas las estaciones de servicio actualmente en la base de datos.
	 *
	 * @return {@link Collection} con todas las {@link EstacionDeServicio} en la base de datos.
	 * @throws IllegalStateException si ocurre un error al obtener las estaciones de servicio.
	 */
	private Collection<EstacionDeServicio> getEstacionesDeServicioEnBD() {
		Collection<EstacionDeServicio> estacionDeServiciosEnBD;
		try {
			long tiempoInicio = System.currentTimeMillis();
			estacionDeServiciosEnBD = servicioEESS.findAllEESS();
			datoDeEjecucion.setTiempoObtenerEESSBD(
					System.currentTimeMillis() - tiempoInicio);
		} catch (Exception e) {
			loggerCron.error("Error al intentar obtener las EESS iniciales.{}",
							 e.getMessage(), e);
			throw new IllegalStateException(
					"Error al intentar obtener las EESS iniciales.", e);
		}
		datoDeEjecucion.setTotalEESSEnBDAlInicio(estacionDeServiciosEnBD.size());
		return estacionDeServiciosEnBD;
	}

	/**
	 * Comprueba, de entre las estaciones de servicio parseadas que ya se encuentran en la
	 * base de datos, cuáles requieren actualización. Para ello compara cada estación de servicio
	 * parseada con la misma estación ya en base de datos.
	 *
	 * @param eessAComprobar       {@link EstacionDeServicio} a comprobar
	 * @param eessDeLaBDPorExtCode {@link Map<>} ya en base de datos, indexadas por su extCode
	 * @return {@link Collection} con las estaciones de servicio parseadas que requieren
	 * actualización.
	 */
	private Collection<EstacionDeServicio> doComprobarEESSaActualizar(
			Collection<EstacionDeServicio> eessAComprobar,
			Map<Integer, EstacionDeServicio> eessDeLaBDPorExtCode) {
		if (eessAComprobar == null) throw new IllegalArgumentException(
				"No se puede comprobar si las EESS requieren actualización si si la COLECCIÓN de EESS en BD es NULA.");

		//Defino la salida
		Collection<EstacionDeServicio> estacionesPendientesActualizacion = new ArrayList<>();

		if (eessAComprobar.isEmpty()) {
			loggerCron.warn(
					"COLECCIÓN de EESS que se encuentran en BD se encuentra VACÍA. No se requiere actualización de ninguna.");
			// Los valores de datoDeEjecucion ya están a 0 por defecto
			return estacionesPendientesActualizacion;
		}

		{ // Medición del tiempo de procesamiento
			long tiempoInicio = System.currentTimeMillis();
			estacionesPendientesActualizacion = eessAComprobar.stream()
					.filter(eessParseada -> {
						EstacionDeServicio eessBD = eessDeLaBDPorExtCode.get(
								eessParseada.getExtCode());
						return eessBD != null &&
								requierenActualizacion(eessBD, eessParseada);
					}).toList();
			datoDeEjecucion.setTiempoProcesamientoEESSRequierenActualizacion(
					System.currentTimeMillis() - tiempoInicio);
		}

		// Definir tiempo usado, monto total de salida y la salida
		datoDeEjecucion.setTotalEESSParseadasRequierenActualizacion(
				estacionesPendientesActualizacion.size());
		loggerCron.info(
				"PROCESADAS {} EESS de la BD (en función de las parseadas) susceptibles de actualización. Se concluye que {} requieren actualización. En {} ms. ",
				datoDeEjecucion.getTotalEESSParseadasFueraDeBD(),
				datoDeEjecucion.getTotalEESSParseadasRequierenActualizacion(),
				datoDeEjecucion.getTiempoProcesamientoEESSRequierenActualizacion());
		return estacionesPendientesActualizacion;
	}


	/**
	 * Encargado de definir si una estacion de servicio ya en base de datos requiere
	 * actualización comparandola con la misma estación parseada. Comprueba:
	 * - String rotulo
	 * - String horario
	 * - String direccion
	 * - String localidad
	 * - int codigoPostal
	 * - Municipio municipio
	 * - Provincia provincia
	 * - double latitud
	 * - double longitud
	 * - Margen margen
	 * - Remision remision
	 * - Venta venta
	 * - double x100BioEtanol
	 * - double x100EsterMetilico
	 *
	 * @param eessEnBD     EESS ya en base de datos
	 * @param eessParseada EESS parseada a comparar
	 * @return true si requieren actualización, false en caso contrario
	 */
	private boolean requierenActualizacion(EstacionDeServicio eessEnBD,
										   EstacionDeServicio eessParseada) {
		boolean actualizacion;

		actualizacion = !Objects.equals(eessEnBD.getRotulo(), eessParseada.getRotulo())
				// Rotulo
				|| !Objects.equals(eessEnBD.getHorario(), eessParseada.getHorario())
				// Horario
				|| !Objects.equals(eessEnBD.getDireccion(), eessParseada.getDireccion())
				// Dirección
				|| !Objects.equals(eessEnBD.getLocalidad(), eessParseada.getLocalidad())
				// Localidad
				|| eessEnBD.getCodigoPostal() != eessParseada.getCodigoPostal()
				// Código postal
				|| !Objects.equals(eessEnBD.getMunicipio(), eessParseada.getMunicipio())
				// Municipio
				|| !Objects.equals(eessEnBD.getProvincia(), eessParseada.getProvincia())
				// Provincia
				|| Double.compare(eessEnBD.getLatitud(), eessParseada.getLatitud()) != 0
				// Latitud
				|| Double.compare(eessEnBD.getLongitud(), eessParseada.getLongitud()) != 0
				// Longitud
				|| !Objects.equals(eessEnBD.getMargen(), eessParseada.getMargen())
				// Margen
				|| !Objects.equals(eessEnBD.getRemision(), eessParseada.getRemision())
				// Remisión
				|| !Objects.equals(eessEnBD.getVenta(), eessParseada.getVenta())
				// Venta
				|| Double.compare(eessEnBD.getX100BioEtanol(),
								  eessParseada.getX100BioEtanol()) != 0
				// Bioetanol
				|| Double.compare(eessEnBD.getX100EsterMetilico(),
								  eessParseada.getX100EsterMetilico()) !=
				0; // Éster metílico
		return actualizacion;
	}


	/**
	 * Inserta en base de datos cada una de las estaciones de servicio pasadas por
	 * parámetro como coleccion, junto con sus respectivos precios y disponibilidades.
	 * <p>
	 * Por motivos de eficiencia esté metodo no comprueba si las estaciones de servicio
	 * están en la base datos o no, se asume que no lo están. Por consecuencia tanto el
	 * combustible disponible de la eess comoo el precio de combustible tampoco estarán en la base de datos.
	 *
	 * @param estacionesAInsertar {@link Collection} que contiene las estaciones de servicio
	 */
	private void doInsertEESSDisponibilidadYPreciosFueraDeBD(
			Collection<EstacionDeServicio> estacionesAInsertar) {
		if (estacionesAInsertar == null) throw new IllegalArgumentException(
				"No se pueden INSERTAR estaciones que no están en la BD si la coleccion de EESS es NULA.");
		if (estacionesAInsertar.isEmpty()) {
			loggerCron.warn(
					"COLECCIÓN de EESS que no se encuentran en BD se encuentra VACÍA. No se ha insertado ninguna.");
			// Los valores de datoDeEjecucion ya están a 0 por defecto
			return;
		}

		// Insertamos todas las estaciones de servicio en bloque
		{
			long tiempoInicio = System.currentTimeMillis();
			datoDeEjecucion.setTotalEESSPrecioYDisponibilidadFueraDeBDInsertadas(
					servicioEESS.addAllEESSandDisponCombusAndPrecioCombus(
							estacionesAInsertar));
			datoDeEjecucion.setTiempoPersistenciaEESSPrecioYDisponibilidadFueraDeBD(
					System.currentTimeMillis() - tiempoInicio);
		}

		// Definir el total de eess insertadas correctamente, y el tiempo usado
		loggerCron.info(
				"INSERTADAS correctamente {} EESS que no estaban en la BD de las {} planteadas en {} ms. ",
				datoDeEjecucion.getTotalEESSPrecioYDisponibilidadFueraDeBDInsertadas(),
				datoDeEjecucion.getTotalEESSParseadasFueraDeBD(),
				datoDeEjecucion.getTiempoPersistenciaEESSPrecioYDisponibilidadFueraDeBD());
	}

	/**
	 * Actualiza en base de datos cada una de las estaciones de servicio pasadas por
	 * parámetro como colección.
	 *
	 * @param coleccionEESSaActualizar {@link Collection} que contiene las estaciones de servicio a actualizar
	 */
	private void doUpdateEESS(Collection<EstacionDeServicio> coleccionEESSaActualizar) {
		if (coleccionEESSaActualizar == null) throw new IllegalArgumentException(
				"No se pueden ACTUALIZAR las estaciones que la coleccion de EESS es NULA.");
		if (coleccionEESSaActualizar.isEmpty()) {
			loggerCron.warn(
					"COLECCIÓN de EESS a actualizar se encuentra VACÍA. No se ha actualizado ninguna.");
			// Los valores de datoDeEjecucion ya están a 0 por defecto
			return;
		}

		// Insertamos todas las estaciones de servicio en bloque
		long tiempoInicio = System.currentTimeMillis();

		for (EstacionDeServicio eess : coleccionEESSaActualizar) {
			//servicioEESS.updateEESS(eess); TODO
			datoDeEjecucion.totalEESSActualizadas++;
		}
		datoDeEjecucion.setTiempoPersistenciaEESSActualizacion(
				System.currentTimeMillis() - tiempoInicio);

		// Definir el total de eess insertadas correctamente, y el tiempo usado
		loggerCron.info(
				"ACTUALIZADAS correctamente {} EESS de la BD de las {} planteadas en {} ms. ",
				datoDeEjecucion.getTotalEESSParseadasRequierenActualizacion(),
				datoDeEjecucion.getTotalEESSActualizadas(),
				datoDeEjecucion.getTiempoPersistenciaEESSActualizacion());
	}

	/**
	 * Se encarga de retornar una colección de objetos CombustibleDisponibleDTO,
	 * básicamente es un registro con el id del combustible y el id de la eess,
	 * Esta contiene todas las disponibilidades de las estaciones de servicio que están
	 * definidas en la colección pasada por parámetro (que no se encuentran en la base de datos).
	 * <p>
	 * La eessAcomprobar ya tendrá asignado su id correspondiente de base de datos. Por lo
	 * que se hará una petición a la base de datos para obtener todos los registros de
	 * disponibilidad de combustible. Tener cuidado con el tratamiento de la disponibilidad
	 * ya que supone trabajar con unos 40.000 registros.
	 *
	 * @param eessAComprobarDisponibilidad {@link Collection} de {@link EstacionDeServicio} a comprobar su disponibilidad
	 * @return {@link Collection} de {@link CombustibleDisponibleDTO} con las disponibilidades a insertar.
	 */
	private Collection<CombustibleDisponibleDTO> getDisponibilidadAInsertar(
			Collection<EstacionDeServicio> eessAComprobarDisponibilidad) {
		// Comprobaciones
		if (eessAComprobarDisponibilidad == null) throw new IllegalArgumentException(
				"No se puede definir objetos Combustible-Disponible si la COLECCIÓN de EESS en BD es NULA.");

		if (eessAComprobarDisponibilidad.isEmpty()) {
			loggerCron.warn(
					"COLECCIÓN de EESS que se encuentran en BD se encuentra VACÍA. No se requiere definir ningún combustible disponible.");
			// Los valores de datoDeEjecucion ya están a 0 por defecto
			return List.of();
		}

		// Medición del tiempo de procesamiento
		long tiempoInicio = System.currentTimeMillis();
		Collection<CombustibleDisponibleDTO> disponibilidadDeCombutible;
		{
			// Transformamos las disponibilidades de las EESS parseadas a DTOs de CombustibleDisponibleDTO
			List<CombustibleDisponibleDTO> listaCombDispParseados = new ArrayList<>();
			for (EstacionDeServicio eess : eessAComprobarDisponibilidad)
				for (Combustible combustible : eess.getCombustiblesDisponibles())
					listaCombDispParseados.add(
							new CombustibleDisponibleDTO(combustible.getId(),
														 eess.getId()));

			// Map de EESS-ID a lista de DTOs DE OBJETOS PARSEADOS para optimizar la búsqueda
			Map<Integer, List<CombustibleDisponibleDTO>> mapCombDispPARSEADOXIdEess = listaCombDispParseados.stream()
					.collect(Collectors.groupingBy(CombustibleDisponibleDTO::getIdEESS));

			// Obtenemos las disponibilidades que ya están en BD para las EESS a comprobar
			Set<Integer> idsEessParseadasEnBd = mapCombDispPARSEADOXIdEess.keySet();
			Collection<CombustibleDisponibleDTO> dispCombEnBD = servicioEESS.findAllCombustiblesDisponiblesByEESSCollectionIds(
					idsEessParseadasEnBd);

			// Creamos un set con claves DE OBJETOS DE LA BD para rápida comprobación
			Map<Integer, Set<Short>> mapCombDispEnBDXIdEess = dispCombEnBD.stream()
					.collect(Collectors.groupingBy(CombustibleDisponibleDTO::getIdEESS,
												   Collectors.mapping(
														   CombustibleDisponibleDTO::getIdCombustible,
														   Collectors.toSet())));

			// Filtramos los DTOs de combustible disponible que no están en BD
			disponibilidadDeCombutible = listaCombDispParseados.stream().filter(dto -> {
				Set<Short> combustiblesEnBD = mapCombDispEnBDXIdEess.get(dto.getIdEESS());
				return combustiblesEnBD == null ||
						!combustiblesEnBD.contains(dto.getIdCombustible());
			}).toList();
		}
		datoDeEjecucion.setTiempoProcesamientoDisponibilidadDeCombustiblesAInsertar(
				System.currentTimeMillis() - tiempoInicio);

		datoDeEjecucion.setTotalProcesamientoDisponibilidadAInsertar(
				disponibilidadDeCombutible.size());
		loggerCron.info(
				"PROCESADAS {} EESS susceptibles de definir objetos Combustible-Disponible a INSERTAR. Se concluye que {} objetos Combustible-Disponible lo requieren. En {} ms. ",
				datoDeEjecucion.getTotalEESSParseadasEnBD(),
				datoDeEjecucion.getTotalProcesamientoDisponibilidadAInsertar(),
				datoDeEjecucion.getTiempoProcesamientoDisponibilidadDeCombustiblesAInsertar());

		return disponibilidadDeCombutible;
	}

	private void doInsertDisponibilidadCombustibles(
			Collection<CombustibleDisponibleDTO> collectionDisponibilidad) {
		// TODO
	}

	private Collection<PrecioCombustible> getPrecioCombustibleAInsertar(
			Collection<EstacionDeServicio> eessAComprobarPrecios) {
		return List.of(); // TODO
	}

	private void doInsertPrecioCombustibles(
			Collection<PrecioCombustible> preciosAInsertar) {
		// TODO
	}

	private Collection<PrecioCombustible> getPrecioCombustibleAActualizar(
			Collection<EstacionDeServicio> preciosAActualizar) {
		return List.of(); // TODO
	}

	private void doUpdatePrecioCombustibles(
			Collection<PrecioCombustible> collectionDisponibilidad) {
		// TODO
	}


	//	private void persistirBatch(List<ES> listaES) { TODO
	//		// ===========================
	//		// Configuración de batch y multihilo
	//		// ===========================
	//		int numHilos = 4;       // número de hilos concurrentes, seguro para Supabase/Postgres
	//		int batchSize = 100;    // flush/clear cada 100 entidades
	//		int chunkSize = 200;    // tamaño de cada chunk que procesa un hilo
	//
	//		ExecutorService executor = Executors.newFixedThreadPool(numHilos);
	//		List<Future<?>> futures = new ArrayList<>();
	//
	//		// ===========================
	//		// Dividir la lista en chunks
	//		// ===========================
	//		for (int i = 0; i < listaES.size(); i += chunkSize) {
	//			int from = i;
	//			int to = Math.min(i + chunkSize, listaES.size());
	//			// Crear copia para evitar problemas con subList concurrente
	//			List<ES> chunk = new ArrayList<>(listaES.subList(from, to));
	//
	//			// ===========================
	//			// Procesar cada chunk en un hilo
	//			// ===========================
	//			futures.add(executor.submit(() -> {
	//				EntityManager em = JPAUtil.getEntityManager();
	//				EntityTransaction tx = null;
	//
	//				try {
	//					tx = em.getTransaction();
	//					tx.begin();
	//					int count = 0;
	//
	//					for (ES ES : chunk) {
	//						// Persistir o actualizar según corresponda
	//						if (ES.getId() == null) {
	//							em.persist(ES);
	//						} else {
	//							em.merge(ES);
	//						}
	//
	//						// Flush y clear por batch
	//						if (++count % batchSize == 0) {
	//							em.flush();
	//							em.clear(); // cuidado: relaciones desasociadas
	//						}
	//
	//						// Opcional: persistencia de precios si se requiere
	//						// for (PrecioCombustible precio : eess.getPrecios()) {
	//						//     if (precio.getId() == null) em.persist(precio);
	//						//     else em.merge(precio);
	//						// }
	//					}
	//
	//					tx.commit();
	//					loggerCron.info(
	//							"Chunk persistido correctamente. Estaciones procesadas en este hilo: {}",
	//							chunk.size());
	//
	//				} catch (Exception e) {
	//					if (tx != null && tx.isActive()) tx.rollback();
	//					loggerCron.error("Error persistiendo chunk. Transacción abortada: {}",
	//									 e.getMessage(), e);
	//
	//				} finally {
	//					if (em.isOpen()) em.close(); // cierre seguro del EntityManager
	//				}
	//			}));
	//		}
	//
	//		// ===========================
	//		// Esperar a que todos los hilos terminen
	//		// ===========================
	//		for (Future<?> f : futures) {
	//			try {
	//				f.get(); // bloquea hasta que termine cada hilo
	//			} catch (InterruptedException | ExecutionException e) {
	//				loggerCron.error("Error en hilo de persistencia: {}", e.getMessage(), e);
	//			}
	//		}
	//
	//		executor.shutdown();
	//		loggerCron.info("Persistencia multihilo completada. Total estaciones: {}",
	//						listaES.size());
	//	}

	//	public List<ES> parseAll(JSONObject json) {
	//		List<ES> resultado = new ArrayList<>();
	//		List<JSONObject> fallidos = new ArrayList<>();
	//
	//		LocalDate fecha = parseFecha(json);
	//		JSONArray listaEESS = json.optJSONArray(
	//				propertyLoader.getJsonKeyProperty("lista.eess.precio"));
	//		if (listaEESS == null) return resultado;
	//
	//		int totalItems = listaEESS.length();
	//		parseLog.info("<<< Inicio del parseado de EESS >>> Total: {}", totalItems);
	//
	//		// Crear pool de hilos seguro
	//		int numHilos = Math.min(Runtime.getRuntime().availableProcessors(),
	//								Integer.parseInt(propertyLoader.getApplicationProperty(
	//										"parser.max.threads")));
	//
	//		ExecutorService executor = Executors.newFixedThreadPool(numHilos);
	//
	//		//		try {
	//		//			List<CompletableFuture<Void>> futures = new ArrayList<>();
	//		//
	//		//			for (int i = 0; i < totalItems; i++) {
	//		//				final int index = i;
	//		//				final JSONObject item = listaEESS.getJSONObject(i);
	//		//
	//		//				CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
	//		//					try {
	//		//						ES ES = eessParser.parse(item, fecha);
	//		//						if (ES != null) {
	//		//							synchronized (resultado) {
	//		//								resultado.add(ES);
	//		//							}
	//		//						}
	//		//					} catch (Exception e) {
	//		//						synchronized (fallidos) {
	//		//							fallidos.add(item);
	//		//						}
	//		//						parseLog.error("Error al parsear estación json {}: {}",
	//		//									   item.toString(), e.getMessage(), e);
	//		//					}
	//		//				}, executor);
	//		//
	//		//				futures.add(future);
	//		//			}
	//		//
	//		//			// Esperar que todos terminen
	//		//			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
	//		//
	//		//		} finally {
	//		//			// Cerrar correctamente los hilos
	//		//			executor.shutdown();
	//		//			try {
	//		//				if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
	//		//					executor.shutdownNow();
	//		//				}
	//		//			} catch (InterruptedException e) {
	//		//				executor.shutdownNow();
	//		//				Thread.currentThread().interrupt();
	//		//			}
	//		//		}
	//
	//		// Resumen final
	//		int parseados = resultado.size();
	//		int fallidas = fallidos.size();
	//		double porcentajeFallidas = totalItems > 0 ? (fallidas * 100.0 / totalItems) : 0;
	//
	//		StringBuilder resumen = new StringBuilder();
	//		resumen.append("===== RESUMEN Parser EESS =====\n").append("Total EESS en JSON: ")
	//				.append(totalItems).append("\n").append("Parseadas correctamente: ")
	//				.append(parseados).append("\n").append("Fallidas: ").append(fallidas)
	//				.append(" (").append(String.format("%.2f", porcentajeFallidas))
	//				.append("%)\n").append("Número de hilos usados: ").append(numHilos)
	//				.append("\n");
	//
	//		if (!fallidos.isEmpty()) {
	//			resumen.append("JSON de EESS que fallaron al parsear:\n");
	//			for (JSONObject fallo : fallidos) {
	//				resumen.append(fallo.toString()).append("\n");
	//			}
	//		}
	//
	//		resumen.append("===============================\n");
	//		loggerParse.info(resumen.toString());
	//
	//		return resultado;
	//	}

}
