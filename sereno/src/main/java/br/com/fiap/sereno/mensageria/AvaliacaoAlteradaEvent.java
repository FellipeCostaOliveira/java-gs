package br.com.fiap.sereno.mensageria;

import java.time.Instant;

import br.com.fiap.sereno.model.Avaliacao;
import br.com.fiap.sereno.model.EnumTipoAlteracao;

public record AvaliacaoAlteradaEvent(Long avaliacaoId, Long localId, Long usuarioId, EnumTipoAlteracao tipo,
		Instant ocorridoEm) {

	public static AvaliacaoAlteradaEvent de(Avaliacao avaliacao, EnumTipoAlteracao tipo) {
		return new AvaliacaoAlteradaEvent(avaliacao.getId(), avaliacao.getLocal().getId(),
				avaliacao.getUsuario().getId(), tipo, Instant.now());
	}

}
