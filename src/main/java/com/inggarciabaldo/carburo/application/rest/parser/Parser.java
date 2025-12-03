package com.inggarciabaldo.carburo.parser;

import com.inggarciabaldo.carburo.model.entity.eess.Combustible;
import com.inggarciabaldo.carburo.model.entity.eess.ES;
import com.inggarciabaldo.carburo.model.entity.geo.Municipio;
import com.inggarciabaldo.carburo.model.entity.geo.Provincia;
import com.inggarciabaldo.carburo.persistence.repository.RepositoryFactory;
import com.inggarciabaldo.carburo.util.Loggers;
import com.inggarciabaldo.carburo.util.PropertyLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Clase de alto nivel para parsear y actualizar estaciones de servicio (EESS)
 * y sus precios de combustible.
 * <p>
 * Ahora se asegura de:
 * - Cerrar correctamente los hilos
 * - Manejar excepciones sin perder resultados
 * - Evitar pérdidas de recursos
 */
public class Parser {

    private static final Logger parseLog = Loggers.PARSE;
    private static final PropertyLoader propertyLoader = PropertyLoader.getInstance();

    private final EESSParser eessParser;

    public Parser() {
        // Cargar datos de referencia para optimizar parseo
        List<Provincia> todasProvincias = RepositoryFactory.getProvinciaRepository().findAll();
        List<Municipio> todosMunicipios = RepositoryFactory.getMunicipioRepository().findAll();
        List<Combustible> todosCombustibles = RepositoryFactory.getCombustibleRepository().findAll();

        Map<BigDecimal, Provincia> provinciasMap = todasProvincias.stream()
                .collect(Collectors.toMap(Provincia::getExtCode, p -> p));
        Map<BigDecimal, Municipio> municipiosMap = todosMunicipios.stream()
                .collect(Collectors.toMap(Municipio::getExtCode, m -> m));
        Map<String, Combustible> combustiblesMap = todosCombustibles.stream()
                .collect(Collectors.toMap(Combustible::getDenominacion, c -> c));
        Set<BigDecimal> eessExistentes = RepositoryFactory.getEESSRepository().findAllExtCodes();


        this.eessParser = new EESSParser(provinciasMap, municipiosMap, combustiblesMap, eessExistentes);
    }

    /**
     * Método principal que procesa un JSON completo de EESS.
     *
     * @param json JSON recibido de la API con lista de EESS y fecha de precios
     * @return Lista de EESS parseadas
     */
    public List<ES> parseAll(JSONObject json) {
        List<ES> resultado = new ArrayList<>();
        List<JSONObject> fallidos = new ArrayList<>();

        LocalDate fecha = parseFecha(json);
        JSONArray listaEESS = json.optJSONArray(propertyLoader.getJsonKeyProperty("lista.eess.precio"));
        if (listaEESS == null) return resultado;

        int totalItems = listaEESS.length();
        parseLog.info("<<< Inicio del parseado de EESS >>> Total: {}", totalItems);

        // Crear pool de hilos seguro
        int numHilos = Math.min(Runtime.getRuntime().availableProcessors(),
                Integer.parseInt(propertyLoader.getApplicationProperty("parser.max.threads")));

        ExecutorService executor = Executors.newFixedThreadPool(numHilos);

        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (int i = 0; i < totalItems; i++) {
                final int index = i;
                final JSONObject item = listaEESS.getJSONObject(i);

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        ES ES = eessParser.parse(item, fecha);
                        if (ES != null) {
                            synchronized (resultado) {
                                resultado.add(ES);
                            }
                        }
                    } catch (Exception e) {
                        synchronized (fallidos) {
                            fallidos.add(item);
                        }
                        parseLog.error("Error al parsear estación json {}: {}", item.toString(), e.getMessage(), e);
                    }
                }, executor);

                futures.add(future);
            }

            // Esperar que todos terminen
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        } finally {
            // Cerrar correctamente los hilos
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // Resumen final
        int parseados = resultado.size();
        int fallidas = fallidos.size();
        double porcentajeFallidas = totalItems > 0 ? (fallidas * 100.0 / totalItems) : 0;

        StringBuilder resumen = new StringBuilder();
        resumen.append("===== RESUMEN Parser EESS =====\n")
                .append("Total EESS en JSON: ").append(totalItems).append("\n")
                .append("Parseadas correctamente: ").append(parseados).append("\n")
                .append("Fallidas: ").append(fallidas).append(" (").append(String.format("%.2f", porcentajeFallidas)).append("%)\n")
                .append("Número de hilos usados: ").append(numHilos).append("\n");

        if (!fallidos.isEmpty()) {
            resumen.append("JSON de EESS que fallaron al parsear:\n");
            for (JSONObject fallo : fallidos) {
                resumen.append(fallo.toString()).append("\n");
            }
        }

        resumen.append("===============================\n");
        parseLog.info(resumen.toString());

        return resultado;
    }

    /**
     * Extrae la fecha del JSON. Si no está presente, devuelve la fecha actual.
     */
    private LocalDate parseFecha(JSONObject json) {
        try {
            String fechaStr = json.optString(propertyLoader.getJsonKeyProperty("fecha"));
            if (fechaStr == null || fechaStr.isEmpty()) return LocalDate.now();
            return LocalDate.parse(fechaStr.substring(0, 10)); // yyyy-MM-dd
        } catch (Exception e) {
            return LocalDate.now();
        }
    }
}
