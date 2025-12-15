package com.inggarciabaldo.carburo.application;


import com.inggarciabaldo.carburo.application.model.enums.fromcode.EnumFromCodeFactory;
import com.inggarciabaldo.carburo.application.model.enums.fromcode.EnumFromCodeFactoryInterface;
import com.inggarciabaldo.carburo.application.model.enums.fromcode.FromCode;
import com.inggarciabaldo.carburo.application.persistance.PersistenceFactory;
import com.inggarciabaldo.carburo.application.service.ServiceFactory;

/**
 * Clase centralizadora de factorías de la aplicación.
 * <p>
 * Esta clase actúa como punto de acceso único a las distintas
 * factorías del sistema, evitando la creación repetida de
 * instancias y facilitando su reutilización.
 * <p>
 * Todas las factorías se exponen como atributos estáticos,
 * funcionando de facto como singletons a nivel de aplicación.
 */
public class Factorias {

	/**
	 * Factoría encargada de la capa de persistencia.
	 * <p>
	 * Proporciona acceso a los distintos repositorios o
	 * componentes relacionados con la gestión de datos
	 * (base de datos, ficheros, etc.).
	 */
	public static final PersistenceFactory persistence = new PersistenceFactory();

	/**
	 * Factoría de utilidades para la obtención de enums a partir de códigos.
	 * <p>
	 * Permite acceder a herramientas {@link FromCode} específicas
	 * para cada enumerado del dominio, centralizando la lógica
	 * de conversión y validación.
	 */
	public static final EnumFromCodeFactoryInterface enumFromCode = new EnumFromCodeFactory();

	/**
	 * Factoría encargada de la capa de servicios.
	 * <p>
	 * Proporciona acceso a los distintos servicios
	 * que implementan la lógica de negocio de la aplicación.
	 */
	public static final ServiceFactory service = new ServiceFactory();
}
