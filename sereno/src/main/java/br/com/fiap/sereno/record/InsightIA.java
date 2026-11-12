package br.com.fiap.sereno.record;

import java.util.List;

public record InsightIA(String resumo, String melhorPeriodo, List<String> pontosDeAtencao, List<String> dicas,
		String perfilRecomendado) {

}
