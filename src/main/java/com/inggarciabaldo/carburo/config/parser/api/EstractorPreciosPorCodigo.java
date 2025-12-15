package com.inggarciabaldo.carburo.config.parser.api;

import com.inggarciabaldo.carburo.application.rest.dto.PreciosCombustibleParserDTO;

import java.util.Map;
import java.util.function.Function;

public class EstractorPreciosPorCodigo {

	private EstractorPreciosPorCodigo() {}

	public static final Map<String, Function<PreciosCombustibleParserDTO, String>> PRECIO_EXTRACTORS = Map.ofEntries(

			// ==== GASOLINAS ====
			Map.entry("G95E5", PreciosCombustibleParserDTO::getPrecioGasolina95E5),
			Map.entry("G95E5+", PreciosCombustibleParserDTO::getPrecioGasolina95E5Premium),
			Map.entry("G95E10", PreciosCombustibleParserDTO::getPrecioGasolina95E10),
			Map.entry("G95E25", PreciosCombustibleParserDTO::getPrecioGasolina95E25),
			Map.entry("G95E85", PreciosCombustibleParserDTO::getPrecioGasolina95E85),
			Map.entry("G98E5", PreciosCombustibleParserDTO::getPrecioGasolina98E5),
			Map.entry("G98E10", PreciosCombustibleParserDTO::getPrecioGasolina98E10),
			Map.entry("GREN", PreciosCombustibleParserDTO::getPrecioGasolinaRenovable),

			// ==== GASÓLEOS ====
			Map.entry("GOA", PreciosCombustibleParserDTO::getPrecioGasoleoA),
			Map.entry("GOA+", PreciosCombustibleParserDTO::getPrecioGasoleoPremium),
			Map.entry("GOB", PreciosCombustibleParserDTO::getPrecioGasoleoB),
			Map.entry("DREN", PreciosCombustibleParserDTO::getPrecioDieselRenovable),

			// ==== BIOCOMBUSTIBLES / OTROS ====
			Map.entry("BIO", PreciosCombustibleParserDTO::getPrecioBiodiesel),
			Map.entry("BIE", PreciosCombustibleParserDTO::getPrecioBioetanol),
			Map.entry("MET", PreciosCombustibleParserDTO::getPrecioMetanol),
			Map.entry("AMO", PreciosCombustibleParserDTO::getPrecioAmoniaco),

			// ==== GASES ====
			Map.entry("GLP", PreciosCombustibleParserDTO::getPrecioGLP),
			Map.entry("GNC", PreciosCombustibleParserDTO::getPrecioGasNaturalComprimido),
			Map.entry("GNL", PreciosCombustibleParserDTO::getPrecioGasNaturalLicuado),
			Map.entry("BGNC",
					  PreciosCombustibleParserDTO::getPrecioBiogasNaturalComprimido),
			Map.entry("BGNL", PreciosCombustibleParserDTO::getPrecioBiogasNaturalLicuado),

			// ==== OTROS ====
			Map.entry("ADB", PreciosCombustibleParserDTO::getPrecioAdblue),
			Map.entry("H2", PreciosCombustibleParserDTO::getPrecioHidrogeno));

}
