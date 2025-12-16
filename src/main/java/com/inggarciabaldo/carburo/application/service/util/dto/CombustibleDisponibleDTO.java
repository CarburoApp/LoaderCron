package com.inggarciabaldo.carburo.application.service.util.dto;

import lombok.Getter;

@SuppressWarnings("CanBeFinal")
@Getter
public class CombustibleDisponibleDTO {

	// DTO
	public short idCombustible;
	public int idEESS;

	public CombustibleDisponibleDTO(short idCombustible, int idEESS) {
		this.idCombustible = idCombustible;
		this.idEESS        = idEESS;
	}
}
