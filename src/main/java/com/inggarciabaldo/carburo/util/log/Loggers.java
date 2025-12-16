package com.inggarciabaldo.carburo.util.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import com.inggarciabaldo.carburo.util.properties.PropertyLoader;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Loggers {

    // ---------------------------------------------------------
    // Constantes de configuración
    // ---------------------------------------------------------
    private static final String PROP_LOG_DIR = "log.directory";
    private static final String DEFAULT_LOG_DIR = "./logs";

    private static final String PROP_LOG_PATTERN = "log.pattern";
    private static final String DEFAULT_LOG_PATTERN = "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n";

    private static final String PROP_LOG_FILE_MAIN = "log.file.main";
    private static final String DEFAULT_LOG_FILE_MAIN = "main.log";

    private static final String LOGGER_GENERAL_NAME = "GENERAL";
    private static final String LOGGER_CRON_NAME = "CRON";
    private static final String LOGGER_PARSE_NAME = "PARSE";
    private static final String LOGGER_DB_NAME = "DB";

    // ---------------------------------------------------------
    // Loggers públicos
    // ---------------------------------------------------------
    public static final Logger GENERAL;
    public static final Logger CRON;
    public static final Logger PARSE;
    public static final Logger DB;

    // ---------------------------------------------------------
    // Appender de ejecución (único por run)
    // ---------------------------------------------------------
    private static FileAppender<ILoggingEvent> execFileAppender;

    private Loggers() {}

    static {
        LoggerContext context = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();

        // Inicializar loggers
        GENERAL = context.getLogger(LOGGER_GENERAL_NAME);
        CRON    = context.getLogger(LOGGER_CRON_NAME);
        PARSE   = context.getLogger(LOGGER_PARSE_NAME);
        DB      = context.getLogger(LOGGER_DB_NAME);

        String logDir = PropertyLoader.getInstance()
                .getApplicationProperty(PROP_LOG_DIR, DEFAULT_LOG_DIR).trim();
        String logPattern = PropertyLoader.getInstance()
                .getApplicationProperty(PROP_LOG_PATTERN, DEFAULT_LOG_PATTERN);
        String mainLogFile = PropertyLoader.getInstance()
                .getApplicationProperty(PROP_LOG_FILE_MAIN, DEFAULT_LOG_FILE_MAIN);

        // Encoder común
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(logPattern);
        encoder.start();

        // Appender consola
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();

        // Appender main.log
        FileAppender<ILoggingEvent> mainFileAppender = new FileAppender<>();
        mainFileAppender.setContext(context);
        mainFileAppender.setEncoder(encoder);
        mainFileAppender.setAppend(true);
        mainFileAppender.setImmediateFlush(true);
        mainFileAppender.setFile(logDir + "/" + mainLogFile);
        mainFileAppender.start();

        // Configurar loggers para main
        configureLoggerForMain(GENERAL, consoleAppender, mainFileAppender);
        configureLoggerForMain(CRON, consoleAppender, mainFileAppender);
        configureLoggerForMain(PARSE, consoleAppender, mainFileAppender);
        configureLoggerForMain(DB, consoleAppender, mainFileAppender);

        GENERAL.info("Loggers inicializados. Consola y main.log en: {}", logDir);
    }

    private static void configureLoggerForMain(ch.qos.logback.classic.Logger logger,
                                               ConsoleAppender<ILoggingEvent> console,
                                               FileAppender<ILoggingEvent> file) {
        logger.setLevel(Level.INFO);
        if (console != null) logger.addAppender(console);
        if (file != null) logger.addAppender(file);
        logger.setAdditive(false);
    }

    /**
     * Crear un appender único para cada ejecución de cron job.
     */
    public static FileAppender<ILoggingEvent> createCronExecutionAppender(
            LocalDateTime timestamp) {
        LoggerContext context = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
        String logDir = PropertyLoader.getInstance()
                .getApplicationProperty(PROP_LOG_DIR, DEFAULT_LOG_DIR).trim();
        String fileName = String.format("cron_job_exec_%s.log", timestamp.format(
                DateTimeFormatter.ofPattern("yyyy_MM_dd_(HH..mm)")));

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(DEFAULT_LOG_PATTERN);
        encoder.start();

        FileAppender<ILoggingEvent> appender = new FileAppender<>();
        appender.setContext(context);
        appender.setEncoder(encoder);
        appender.setAppend(true);
        appender.setImmediateFlush(true);
        appender.setFile(logDir + "/" + fileName);
        appender.start();

        return appender;
    }
}
