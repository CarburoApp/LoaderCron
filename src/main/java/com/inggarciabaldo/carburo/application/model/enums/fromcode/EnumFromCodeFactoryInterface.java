package com.inggarciabaldo.carburo.application.model.enums.fromcode;

import com.inggarciabaldo.carburo.application.model.enums.Margen;
import com.inggarciabaldo.carburo.application.model.enums.Remision;
import com.inggarciabaldo.carburo.application.model.enums.Venta;

public interface EnumFromCodeFactoryInterface {

	/**
	 * Devuelve la herramienta FromCode asociada al enum {@link Margen}.
	 *
	 * @return instancia singleton de {@link FromCode} para {@link Margen}
	 */
	FromCode<Margen> getMargenFromCodeTool();

	/**
	 * Devuelve la herramienta FromCode asociada al enum {@link Remision}.
	 *
	 * @return instancia singleton de {@link FromCode} para {@link Remision}
	 */
	FromCode<Remision> getRemisionFromCodeTool();

	/**
	 * Devuelve la herramienta FromCode asociada al enum {@link Venta}.
	 *
	 * @return instancia singleton de {@link FromCode} para {@link Venta}
	 */
	FromCode<Venta> getVentaFromCodeTool();

}
