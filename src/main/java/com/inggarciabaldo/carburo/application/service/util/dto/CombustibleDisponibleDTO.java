package com.inggarciabaldo.carburo.application.service.util.dto;

public class CombustibleDisponibleDTO {

	// DTO
	public short idCombustible;
	public int idEESS;

	public CombustibleDisponibleDTO(short idCombustible, int idEESS) {
		this.idCombustible = idCombustible;
		this.idEESS        = idEESS;
	}

	//Getter

	public int getIdEESS() {return idEESS;}

	public short getIdCombustible() {return idCombustible;}
}
