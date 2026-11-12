package br.com.fiap.sereno.record;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.fiap.sereno.model.EnumCategoria;
import br.com.fiap.sereno.model.Local;
import br.com.fiap.sereno.service.IndiceCalmaCalculadora;

public record LocalResponse(Long id, String nome, String descricao, EnumCategoria categoria, String endereco,
		BigDecimal latitude, BigDecimal longitude, String recursosAcessibilidade, BigDecimal indiceCalma,
		String classificacaoCalma, Integer totalAvaliacoes, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {

	public static LocalResponse de(Local local) {
		return new LocalResponse(local.getId(), local.getNome(), local.getDescricao(), local.getCategoria(),
				local.getEndereco(), local.getLatitude(), local.getLongitude(), local.getRecursosAcessibilidade(),
				local.getIndiceCalma(), IndiceCalmaCalculadora.classificar(local.getIndiceCalma()),
				local.getTotalAvaliacoes(), local.getCriadoEm(), local.getAtualizadoEm());
	}

}
