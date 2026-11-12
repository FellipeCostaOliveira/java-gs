package br.com.fiap.sereno.record;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.fiap.sereno.model.Avaliacao;
import br.com.fiap.sereno.model.EnumPeriodo;
import br.com.fiap.sereno.service.IndiceCalmaCalculadora;

public record AvaliacaoResponse(Long id, Long localId, String nomeLocal, Long usuarioId, String nomeUsuario,
		Integer nivelRuido, Integer nivelIluminacao, Integer nivelLotacao, EnumPeriodo periodoVisita,
		String comentario, BigDecimal indiceCalmaDaAvaliacao, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {

	public static AvaliacaoResponse de(Avaliacao a) {
		return new AvaliacaoResponse(a.getId(), a.getLocal().getId(), a.getLocal().getNome(), a.getUsuario().getId(),
				a.getUsuario().getNome(), a.getNivelRuido(), a.getNivelIluminacao(), a.getNivelLotacao(),
				a.getPeriodoVisita(), a.getComentario(),
				IndiceCalmaCalculadora.calcular(a.getNivelRuido(), a.getNivelIluminacao(), a.getNivelLotacao()),
				a.getCriadoEm(), a.getAtualizadoEm());
	}

}
