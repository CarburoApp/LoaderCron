package com.inggarciabaldo.carburo.scheduler.jobs;

import com.inggarciabaldo.carburo.application.Factorias;
import com.inggarciabaldo.carburo.application.model.Combustible;
import com.inggarciabaldo.carburo.application.model.EstacionDeServicio;
import com.inggarciabaldo.carburo.application.model.PrecioCombustible;
import com.inggarciabaldo.carburo.application.service.eess.EESSCrudService;
import com.inggarciabaldo.carburo.application.service.util.dto.CombustibleDisponibleDTO;
import com.inggarciabaldo.carburo.util.log.Loggers;
import org.slf4j.Logger;

import java.time.LocalDate;
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
	private final DatosDeEjecucion datosDeEjecucion;
	private final Collection<EstacionDeServicio> estacionDeServicioParseada;

	public ProcesadoDePersistenciaEESSaBD(
			Collection<EstacionDeServicio> estacionDeServicioParseada,
			DatosDeEjecucion datosDeEjecucion) {
		// Comprobaciones
		if (datosDeEjecucion == null) throw new IllegalArgumentException(
				"No se puede procesar la persistencia de eess ya que el objeto de datos de ejecución es nulo.");
		if (estacionDeServicioParseada == null) throw new IllegalArgumentException(
				"No se puede procesar la persistencia de eess ya que la colección introducida es NULA.");

		if (estacionDeServicioParseada.isEmpty()) {
			loggerCron.warn("COLECCIÓN de EESS recibida para persistir está VACÍA.");

		}

		// Objeto de estadísticas.
		this.datosDeEjecucion = datosDeEjecucion;

		// Ordenamos las estaciones por su extCode
		this.estacionDeServicioParseada = estacionDeServicioParseada.stream()
				.sorted(Comparator.comparing(EstacionDeServicio::getExtCode)).toList();

		// Inicializo el servicio de persistencia
		this.servicioEESS = Factorias.service.forEESS();
	}


	/**
	 * Comparará aquellas estaciones de servicio parseadas,
	 * actualmente introducidas por parámetro con las ya existentes en la base de datos.
	 * Si no existe la estación de servicio, se persistirá. Si ya existe, se comprobará si
	 * alguno de sus datos ha cambiado y en caso afirmativo se actualizará.
	 * <p>
	 * Aparte, comprobará los precios de cada estación de servicio y, si no existen en la base de datos, los persistirá. En caso de que existan, comprobará si han cambiado y los actualizará en caso afirmativo.
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

		datosDeEjecucion.setDecisionEESSNuevas(coleccionEESSFueraDeBD.size());
		datosDeEjecucion.setDecisionEESSYaPresentes(coleccionEESSPresentesEnBD.size());

		loggerCron.info(
				"Separación de las EESS parseadas en función de si EXISTE en la BD: Sí - {} EESS; No - {} EESS.",
				datosDeEjecucion.getDecisionEESSYaPresentes(),
				datosDeEjecucion.getDecisionEESSNuevas());

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
						"COLECCIÓN de combustibles disponibles de EESS que ya se encontraban en la BD a INSERTAR está VACÍA. No se requiere persistir la disponibilidad de ningún combustible.");

			} catch (Exception e) {
				IllegalStateException exp = new IllegalStateException(
						"Error inesperado al intentar definir y persistir la disponibilidad de los combustibles de las EESS que ya están en BD. Por tanto no se ha podido completar el proceso de procesamiento.",
						e);
				loggerCron.error(
						"Error en procesamiento y persistencia de combustibles disponibles de eess que ya se encuentran en la BD. {}",
						e.getMessage(), e);
				throw exp;
			}

			try {
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
			} catch (Exception e) {
				IllegalStateException exp = new IllegalStateException(
						"Error inesperado al intentar procesar o persistir los Precios de los combustibles de las EESS que ya están en BD. Por tanto no se ha podido completar el proceso de procesamiento.",
						e);
				loggerCron.error(
						"Error en procesamiento y persistencia de los precios de los combustibles  de eess que ya se encuentran en la BD. {}",
						e.getMessage(), e);
				throw exp;
			}
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
			datosDeEjecucion.setBdCargaInicialEESSTiempoMs(
					System.currentTimeMillis() - tiempoInicio);
		} catch (Exception e) {
			loggerCron.error("Error al intentar obtener las EESS iniciales.{}",
							 e.getMessage(), e);
			throw new IllegalStateException(
					"Error al intentar obtener las EESS iniciales.", e);
		}
		datosDeEjecucion.setBdTotalEESSIniciales(estacionDeServiciosEnBD.size());
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
			datosDeEjecucion.setDecisionEESSActualizarTiempoMs(
					System.currentTimeMillis() - tiempoInicio);
		}

		// Definir tiempo usado, monto total de salida y la salida
		datosDeEjecucion.setDecisionEESSActualizar(
				estacionesPendientesActualizacion.size());
		loggerCron.info(
				"PROCESADAS {} EESS (aquellas que parseamos y ya se encontraban presentes en la BD) susceptibles de ACTUALIZACIÓN. Se concluye que {} requieren actualización. En {} ms. ",
				datosDeEjecucion.getDecisionEESSYaPresentes(),
				datosDeEjecucion.getDecisionEESSActualizar(),
				datosDeEjecucion.getDecisionEESSActualizarTiempoMs());
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
			datosDeEjecucion.setPersistenciaEESSInsertadas(
					servicioEESS.addAllEESSandDisponCombusAndPrecioCombus(
							estacionesAInsertar));
			datosDeEjecucion.setPersistenciaEESSInsertTiempoMs(
					System.currentTimeMillis() - tiempoInicio);
		}

		// Definir el total de eess insertadas correctamente, y el tiempo usado
		loggerCron.info(
				"INSERTADAS correctamente {} EESS que no estaban en la BD de las {} planteadas en {} ms. ",
				datosDeEjecucion.getPersistenciaEESSInsertadas(),
				datosDeEjecucion.getDecisionEESSNuevas(),
				datosDeEjecucion.getPersistenciaEESSInsertTiempoMs());
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
			servicioEESS.updateEESS(eess);
			datosDeEjecucion.setPersistenciaEESSActualizadas(
					datosDeEjecucion.getPersistenciaEESSActualizadas() + 1);
		}
		datosDeEjecucion.setPersistenciaEESSUpdateTiempoMs(
				System.currentTimeMillis() - tiempoInicio);

		// Definir el total de eess insertadas correctamente, y el tiempo usado
		loggerCron.info(
				"ACTUALIZADAS correctamente {} EESS de la BD de las {} planteadas en {} ms. ",
				datosDeEjecucion.getPersistenciaEESSActualizadas(),
				datosDeEjecucion.getDecisionEESSActualizar(),
				datosDeEjecucion.getPersistenciaEESSUpdateTiempoMs());
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
		int totalDisponibilidad;
		Collection<CombustibleDisponibleDTO> disponibilidadDeCombutible;
		{
			// Transformamos las disponibilidades de las EESS parseadas a DTOs de CombustibleDisponibleDTO
			List<CombustibleDisponibleDTO> listaCombDispParseados = new ArrayList<>();
			for (EstacionDeServicio eess : eessAComprobarDisponibilidad)
				for (Combustible combustible : eess.getCombustiblesDisponibles())
					listaCombDispParseados.add(
							new CombustibleDisponibleDTO(combustible.getId(),
														 eess.getId()));
			totalDisponibilidad = listaCombDispParseados.size();

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
		datosDeEjecucion.setDecisionDisponibilidadInsertarTiempoMs(
				System.currentTimeMillis() - tiempoInicio);

		datosDeEjecucion.setDecisionDisponibilidadesInsertar(
				disponibilidadDeCombutible.size());
		loggerCron.info(
				"PROCESADOS {} objetos Combustible-Disponible susceptibles de necesitar ser INSERTADOS en las EESS que ya se encontraban en BD ({}). Se concluye que {} objetos Combustible-Disponible lo requieren. En {} ms. ",
				totalDisponibilidad, datosDeEjecucion.getDecisionEESSYaPresentes(),
				datosDeEjecucion.getDecisionDisponibilidadesInsertar(),
				datosDeEjecucion.getDecisionDisponibilidadInsertarTiempoMs());

		return disponibilidadDeCombutible;
	}

	/**
	 * Encargado de realizar la inserción masiva de objetos de {@link CombustibleDisponibleDTO}.
	 *
	 * @param collectionDisponibilidad {@link Collection} que contiene los objetos a insertar
	 */
	private void doInsertDisponibilidadCombustibles(
			Collection<CombustibleDisponibleDTO> collectionDisponibilidad) {
		if (collectionDisponibilidad == null) throw new IllegalArgumentException(
				"No se pueden INSERTAR objetos Combustible-Disponible si la COLECCIÓN es NULA.");
		if (collectionDisponibilidad.isEmpty()) {
			loggerCron.warn(
					"COLECCIÓN de Combustible-Disponible a INSERTAR VACÍA. NO se ha INSERTADO ningÚn Combustible-Disponible.");
			// Los valores de datoDeEjecucion ya están a 0 por defecto
			return;
		}

		// Insertamos todos los estaciones de servicio en bloque
		{
			long tiempoInicio = System.currentTimeMillis();
			datosDeEjecucion.setPersistenciaDisponibilidadesInsertadas(
					servicioEESS.addAllCombustiblesDisponibles(collectionDisponibilidad));
			datosDeEjecucion.setPersistenciaDisponibilidadesInsertTiempoMs(
					System.currentTimeMillis() - tiempoInicio);
		}

		// Definir el total de objetos disponibilidad-combustible insertados correctamente, y el tiempo usado
		loggerCron.info(
				"INSERTADAS correctamente {} objetos Combustible-Disponible que no estaban en la BD de los {} planteadas en {} ms.",
				datosDeEjecucion.getPersistenciaDisponibilidadesInsertadas(),
				collectionDisponibilidad.size(),
				datosDeEjecucion.getPersistenciaDisponibilidadesInsertTiempoMs());
	}

	private Collection<PrecioCombustible> getPrecioCombustibleAInsertar(
			Collection<EstacionDeServicio> eessAComprobarPrecios) {
		// Comprobaciones
		if (eessAComprobarPrecios == null) throw new IllegalArgumentException(
				"No se puede definir objetos Precio-Combustible si la COLECCIÓN de EESS en BD es NULA.");

		if (eessAComprobarPrecios.isEmpty()) {
			loggerCron.warn(
					"COLECCIÓN de EESS que se encuentran en BD se encuentra VACÍA. No se requiere definir ningún Precio-Combustible.");
			// Los valores de datoDeEjecucion ya están a 0 por defecto
			return List.of();
		}

		final LocalDate fecha = datosDeEjecucion.getFechaDeDatosProcesados();
		int totalPreciosPosiblesAInsertar;
		Collection<PrecioCombustible> precioCombustiblesAInsertar;

		// Medición del tiempo de procesamiento
		long tiempoInicio = System.currentTimeMillis();
		{
			// 1.  Transformamos las disponibilidades de las EESS parseadas a objetos de PrecioCombustible del día del Parseo
			List<PrecioCombustible> listaPrecioCombustibleParseados = eessAComprobarPrecios.stream()
					.flatMap(eess -> eess.getPreciosCombustibles().stream())
					.filter(pc -> fecha.equals(pc.getFecha())).toList();
			totalPreciosPosiblesAInsertar = listaPrecioCombustibleParseados.size();

			// 2. IDs de EESS implicada
			Map<Integer, EstacionDeServicio> mapIdsEessParseadas = listaPrecioCombustibleParseados.stream()
					.collect(Collectors.toMap(pc -> pc.getEstacionDeServicio().getId(),
											  PrecioCombustible::getEstacionDeServicio,
											  (existing, replacement) -> existing));


			// 3. Precios ya existentes en BD para esas EESS y fecha
			Collection<PrecioCombustible> precioCombustiblesEnBD = servicioEESS.findAllPrecioCombustibleByEESSCollectionIdsAndFecha(
					mapIdsEessParseadas, fecha);

			// Creamos un set con claves DE OBJETOS DE LA BD para rápida comprobación
			// 4. Mapa de BD: EESS_ID -> Set<Combustible> ya que así tenemos las 3 claves primarias
			// -- Fecha (dada), idEess es la clave Integer y el combustible. Fecha fija, y map eeess -> Comb
			Map<Integer, Set<Combustible>> mapCombustiblesEnBDXIdEess = precioCombustiblesEnBD.stream()
					.collect(Collectors.groupingBy(
							pc -> pc.getEstacionDeServicio().getId(),
							Collectors.mapping(PrecioCombustible::getCombustible,
											   Collectors.toSet())));

			// 5. Filtramos los precios parseados que NO están en BD
			precioCombustiblesAInsertar = listaPrecioCombustibleParseados.stream()
					.filter(pc -> {
						Set<Combustible> combustiblesEnBD = mapCombustiblesEnBDXIdEess.get(
								pc.getEstacionDeServicio().getId());

						return combustiblesEnBD == null ||
								!combustiblesEnBD.contains(pc.getCombustible());
					}).toList();
		}

		datosDeEjecucion.setDecisionPreciosInsertarTiempoMs(
				System.currentTimeMillis() - tiempoInicio);

		datosDeEjecucion.setDecisionPreciosInsertar(
				precioCombustiblesAInsertar.size());
		loggerCron.info(
				"PROCESADOS {} objetos Precio-Combustible susceptibles de necesitar ser INSERTAR en las EESS que ya se encontraban en BD ({}). Se concluye que {} objetos Precio-Combustible lo requieren. En {} ms. ",
				totalPreciosPosiblesAInsertar,
				datosDeEjecucion.getDecisionEESSYaPresentes(),
				datosDeEjecucion.getDecisionPreciosInsertar(),
				datosDeEjecucion.getDecisionPreciosInsertarTiempoMs());

		return precioCombustiblesAInsertar;
	}

	/**
	 * Encargado de realizar la inserción masiva de objetos de {@link PrecioCombustible}.
	 *
	 * @param preciosAInsertar {@link Collection} con los objetos a insertar.
	 */
	private void doInsertPrecioCombustibles(
			Collection<PrecioCombustible> preciosAInsertar) {
		if (preciosAInsertar == null) throw new IllegalArgumentException(
				"No se pueden INSERTAR objetos Precio-Combustible si la COLECCIÓN es NULA.");
		if (preciosAInsertar.isEmpty()) {
			loggerCron.warn(
					"COLECCIÓN de Precio-Combustible a INSERTAR VACÍA. NO se ha INSERTADO ningÚn Precio-Combustible.");
			// Los valores de datoDeEjecucion ya están a 0 por defecto
			return;
		}

		// Insertamos todos los precios en bloque
		{
			long tiempoInicio = System.currentTimeMillis();
			datosDeEjecucion.setPersistenciaPreciosInsertados(
					servicioEESS.addAllPrecioCombustibles(preciosAInsertar));
			datosDeEjecucion.setPersistenciaPreciosInsertTiempoMs(
					System.currentTimeMillis() - tiempoInicio);
		}

		// Definir el total de precios insertados correctamente, y el tiempo usado
		loggerCron.info(
				"INSERTADAS correctamente {} objetos Precio-Combustible que no estaban en la BD de los {} planteadas en {} ms.",
				datosDeEjecucion.getPersistenciaPreciosInsertados(),
				preciosAInsertar.size(),
				datosDeEjecucion.getPersistenciaPreciosInsertTiempoMs());
	}

	private Collection<PrecioCombustible> getPrecioCombustibleAActualizar(
			Collection<EstacionDeServicio> eessAComprobarPrecios) {
		// Comprobaciones
		if (eessAComprobarPrecios == null) throw new IllegalArgumentException(
				"No se puede definir objetos Precio-Combustible si la COLECCIÓN de EESS en BD es NULA.");

		if (eessAComprobarPrecios.isEmpty()) {
			loggerCron.warn(
					"COLECCIÓN de EESS que se encuentran en BD se encuentra VACÍA. No se requiere definir ningún Precio-Combustible.");
			// Los valores de datoDeEjecucion ya están a 0 por defecto
			return List.of();
		}

		final LocalDate fecha = datosDeEjecucion.getFechaDeDatosProcesados();

		// Medición del tiempo de procesamiento
		long tiempoInicio = System.currentTimeMillis();
		int totalPreciosAActualizar;
		Collection<PrecioCombustible> precioCombustiblesAActualizar;
		{
			// 1.  Transformamos las disponibilidades de las EESS parseadas a objetos de PrecioCombustible del día del Parseo
			List<PrecioCombustible> listaPrecioCombustibleParseados = eessAComprobarPrecios.stream()
					.flatMap(eess -> eess.getPreciosCombustibles().stream())
					.filter(pc -> fecha.equals(pc.getFecha())).toList();
			totalPreciosAActualizar = listaPrecioCombustibleParseados.size();

			// 2. IDs de EESS implicadas
			Map<Integer, EstacionDeServicio> mapIdsEessParseadas = listaPrecioCombustibleParseados.stream()
					.collect(Collectors.toMap(pc -> pc.getEstacionDeServicio().getId(),
											  PrecioCombustible::getEstacionDeServicio,
											  (existing, replacement) -> existing));

			// 3. Precios ya existentes en BD para esas EESS y fecha
			Collection<PrecioCombustible> precioCombustiblesEnBD = servicioEESS.findAllPrecioCombustibleByEESSCollectionIdsAndFecha(
					mapIdsEessParseadas, fecha);

			// 4. Mapa BD: EESS_ID -> (Combustible -> PrecioCombustible BD) ya que así tenemos las 3 claves primarias y el precio
			// -- Fecha (dada), idEess es la clave Integer y el combustible. Fecha fija, y map eeess -> map_Comb -> precio
			Map<Integer, Map<Combustible, PrecioCombustible>> mapPrecioBDXIdEess = precioCombustiblesEnBD.stream()
					.collect(Collectors.groupingBy(
							pc -> pc.getEstacionDeServicio().getId(),
							Collectors.toMap(PrecioCombustible::getCombustible,
											 pc -> pc)));

			// 5. Filtramos los precios parseados que EXISTEN en BD y tienen PRECIO DISTINTO
			precioCombustiblesAActualizar = listaPrecioCombustibleParseados.stream()
					.filter(pcParseado -> {
						Map<Combustible, PrecioCombustible> preciosBDPorComb = mapPrecioBDXIdEess.get(
								pcParseado.getEstacionDeServicio().getId());
						if (preciosBDPorComb == null) return false;
						PrecioCombustible pcBD = preciosBDPorComb.get(
								pcParseado.getCombustible());
						return pcBD != null && Double.compare(pcBD.getPrecio(),
															  pcParseado.getPrecio()) !=
								0;
					}).toList();
		}

		datosDeEjecucion.setDecisionPreciosActualizarTiempoMs(
				System.currentTimeMillis() - tiempoInicio);

		datosDeEjecucion.setDecisionPreciosActualizar(
				precioCombustiblesAActualizar.size());
		loggerCron.info(
				"PROCESADOS {} objetos Precio-Combustible susceptibles de necesitar ser ACTUALIZADOS en las EESS que ya se encontraban en BD ({}). Se concluye que {} objetos Precio-Combustible lo requieren. En {} ms. ",
				totalPreciosAActualizar, datosDeEjecucion.getDecisionEESSYaPresentes(),
				datosDeEjecucion.getDecisionPreciosActualizar(),
				datosDeEjecucion.getDecisionPreciosActualizarTiempoMs());

		return precioCombustiblesAActualizar;
	}

	/**
	 * Encargado de realizar la actualización masiva de objetos de {@link CombustibleDisponibleDTO}.
	 *
	 * @param preciosAActualizar {@link Collection} con los objetos a actualizar.
	 */
	private void doUpdatePrecioCombustibles(
			Collection<PrecioCombustible> preciosAActualizar) {
		if (preciosAActualizar == null) throw new IllegalArgumentException(
				"No se pueden ACTUALIZAR objetos Precio-Combustible si la COLECCIÓN es NULA.");
		if (preciosAActualizar.isEmpty()) {
			loggerCron.warn(
					"COLECCIÓN de Precio-Combustible a ACTUALIZAR VACÍA. NO se ha ACTUALIZADO ningÚn Precio-Combustible.");
			// Los valores de datoDeEjecucion ya están a 0 por defecto
			return;
		}

		// Actualizamos todos los precios en bloque
		{
			long tiempoInicio = System.currentTimeMillis();
			datosDeEjecucion.setPersistenciaPreciosActualizados(
					servicioEESS.updateAllPrecioCombustibles(preciosAActualizar));
			datosDeEjecucion.setPersistenciaPreciosUpdateTiempoMs(
					System.currentTimeMillis() - tiempoInicio);
		}

		// Definir el total de precios actualizados correctamente, y el tiempo usado
		loggerCron.info(
				"ACTUALIZADOS correctamente {} objetos Precio-Combustible que no estaban en la BD de los {} planteadas en {} ms.",
				datosDeEjecucion.getPersistenciaPreciosActualizados(),
				preciosAActualizar.size(),
				datosDeEjecucion.getPersistenciaPreciosUpdateTiempoMs());
	}
}
