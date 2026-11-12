package br.com.fiap.sereno.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.sereno.record.EstatisticasAvaliacao;
import br.com.fiap.sereno.model.Local;
import br.com.fiap.sereno.repository.AvaliacaoRepository;
import br.com.fiap.sereno.repository.LocalRepository;

@Service
public class IndiceCalmaService {

	private static final Logger log = LoggerFactory.getLogger(IndiceCalmaService.class);

	private final LocalRepository localRepository;
	private final AvaliacaoRepository avaliacaoRepository;
	private final InsightService insightService;

	public IndiceCalmaService(LocalRepository localRepository, AvaliacaoRepository avaliacaoRepository,
			InsightService insightService) {
		this.localRepository = localRepository;
		this.avaliacaoRepository = avaliacaoRepository;
		this.insightService = insightService;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void recalcular(Long localId) {
		Optional<Local> op = localRepository.findById(localId);
		if (op.isEmpty()) {
			log.info("Local {} não existe mais; recálculo ignorado", localId);
			return;
		}
		Local local = op.get();
		EstatisticasAvaliacao estatisticas = avaliacaoRepository.calcularEstatisticas(localId);
		local.setIndiceCalma(estatisticas.indiceCalma());
		local.setTotalAvaliacoes(estatisticas.vazio() ? 0 : estatisticas.total().intValue());
		localRepository.save(local);
		insightService.invalidarCache(localId);
		log.info("indice_calma do local {} recalculado: {} ({} avaliações)", localId, local.getIndiceCalma(),
				local.getTotalAvaliacoes());
	}

}
