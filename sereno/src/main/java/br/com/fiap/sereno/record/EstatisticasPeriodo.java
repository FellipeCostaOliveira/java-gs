package br.com.fiap.sereno.record;

import java.math.BigDecimal;

import br.com.fiap.sereno.model.EnumPeriodo;
import br.com.fiap.sereno.service.IndiceCalmaCalculadora;

public record EstatisticasPeriodo(EnumPeriodo periodo, Long total, Double mediaRuido, Double mediaIluminacao,
		Double mediaLotacao) {

	public BigDecimal indiceCalma() {
		return IndiceCalmaCalculadora.calcular(mediaRuido, mediaIluminacao, mediaLotacao);
	}

}
