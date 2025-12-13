package com.inggarciabaldo.carburo.application.model.enums.fromcode;

import com.inggarciabaldo.carburo.application.model.enums.Margen;
import com.inggarciabaldo.carburo.application.model.enums.Remision;
import com.inggarciabaldo.carburo.application.model.enums.Venta;

/**
 * Implementación de la factoría de herramientas FromCode para enums.
 * <p>
 * Esta clase centraliza la obtención de instancias {@link FromCode}
 * para distintos enumerados del dominio, evitando:
 * - la duplicación de lógica
 * - la creación innecesaria de instancias
 * <p>
 * Cada {@link FromCode} es a su vez un singleton por tipo de enum.
 */
public class EnumFromCodeFactory implements EnumFromCodeFactoryInterface {


	/**
	 * Devuelve la herramienta FromCode asociada al enum {@link Margen}.
	 *
	 * @return instancia singleton de {@link FromCode} para {@link Margen}
	 */
	@Override
	public FromCode<Margen> getMargenFromCodeTool() {
		return FromCode.getInstance(Margen.class);
	}

	/**
	 * Devuelve la herramienta FromCode asociada al enum {@link Remision}.
	 *
	 * @return instancia singleton de {@link FromCode} para {@link Remision}
	 */
	@Override
	public FromCode<Remision> getRemisionFromCodeTool() {
		return FromCode.getInstance(Remision.class);
	}

	/**
	 * Devuelve la herramienta FromCode asociada al enum {@link Venta}.
	 *
	 * @return instancia singleton de {@link FromCode} para {@link Venta}
	 */
	@Override
	public FromCode<Venta> getVentaFromCodeTool() {
		return FromCode.getInstance(Venta.class);
	}
}
