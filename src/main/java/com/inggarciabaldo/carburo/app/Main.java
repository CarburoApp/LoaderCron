package com.inggarciabaldo.carburo.app;

import com.inggarciabaldo.carburo.config.persistencia.jdbc.Jdbc;

public class Main {
	public static void main(String[] args) {

		System.out.println("Iniciando aplicación...");

		// Probar conexión a la BD
		boolean ok = Jdbc.testConnection();

		if (ok) {
			System.out.println("✔ Conexión a la base de datos OK");
		} else {
			System.out.println("✘ Error al conectar con la base de datos");
		}

		System.out.println("Fin del programa.");
	}
}