package br.com.fiap.sereno.record;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record InsightResponse(Long localId, String nomeLocal, BigDecimal indiceCalma, String classificacaoCalma,
		long totalAvaliacoes, List<PeriodoResponse> indicePorPeriodo, String resumo, String melhorPeriodo,
		List<String> pontosDeAtencao, List<String> dicas, String perfilRecomendado, String fonte,
		OffsetDateTime geradoEm) {

}
