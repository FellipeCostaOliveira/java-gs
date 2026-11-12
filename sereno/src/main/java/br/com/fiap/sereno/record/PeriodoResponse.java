package br.com.fiap.sereno.record;

import java.math.BigDecimal;

import br.com.fiap.sereno.model.EnumPeriodo;

public record PeriodoResponse(EnumPeriodo periodo, long totalAvaliacoes, BigDecimal indiceCalma) {

	public static PeriodoResponse de(EstatisticasPeriodo e) {
		return new PeriodoResponse(e.periodo(), e.total(), e.indiceCalma());
	}

}
