package br.com.fiap.sereno.record;

import java.math.BigDecimal;

import br.com.fiap.sereno.service.IndiceCalmaCalculadora;

public record EstatisticasAvaliacao(Long total, Double mediaRuido, Double mediaIluminacao, Double mediaLotacao) {

	public boolean vazio() {
		return total == null || total == 0;
	}

	public BigDecimal indiceCalma() {
		return vazio() ? null : IndiceCalmaCalculadora.calcular(mediaRuido, mediaIluminacao, mediaLotacao);
	}

}
