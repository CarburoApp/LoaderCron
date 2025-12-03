package com.inggarciabaldo.carburo.application;


import com.inggarciabaldo.carburo.application.persistance.PersistenceFactory;

public class Factorias {

	public static PersistenceFactory persistence = new PersistenceFactory();
	//public static ServiceFactory service = new ServiceFactory();

	public static void close() {
	}

}
