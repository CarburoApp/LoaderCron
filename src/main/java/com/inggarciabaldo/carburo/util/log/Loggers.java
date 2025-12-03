package com.inggarciabaldo.carburo.util.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase centralizada para obtener loggers con etiquetas específicas.
 * Evita repetir LoggerFactory.getLogger en todas las clases.
 */
public final class Loggers {

    // Logger para el cron
    public static final Logger CRON = LoggerFactory.getLogger("CRON");

    // Logger para procesos de parsing
    public static final Logger PARSE = LoggerFactory.getLogger("PARSE");

    // Logger para operaciones de base de datos
    public static final Logger DB = LoggerFactory.getLogger("DB");

    // Logger general para otras tareas
    public static final Logger GENERAL = LoggerFactory.getLogger("GENERAL");

    // Constructor privado para evitar instanciación
    private Loggers() {
    }
}

