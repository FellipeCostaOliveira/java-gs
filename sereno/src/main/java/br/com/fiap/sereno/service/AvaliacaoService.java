package br.com.fiap.sereno.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.sereno.record.AvaliacaoRequest;
import br.com.fiap.sereno.record.AvaliacaoResponse;
import br.com.fiap.sereno.record.AvaliacaoUpdateRequest;
import br.com.fiap.sereno.record.PageResponse;
import br.com.fiap.sereno.exception.ConflitoException;
import br.com.fiap.sereno.exception.RecursoNaoEncontradoException;
import br.com.fiap.sereno.mensageria.AvaliacaoAlteradaEvent;
import br.com.fiap.sereno.model.Avaliacao;
import br.com.fiap.sereno.model.EnumTipoAlteracao;
import br.com.fiap.sereno.model.Local;
import br.com.fiap.sereno.model.Usuario;
import br.com.fiap.sereno.repository.AvaliacaoRepository;
import br.com.fiap.sereno.security.UsuarioAutenticado;

@Service
public class AvaliacaoService {

	private final AvaliacaoRepository avaliacaoRepository;
	private final LocalService localService;
	private final UsuarioService usuarioService;
	private final ApplicationEventPublisher eventPublisher;

	public AvaliacaoService(AvaliacaoRepository avaliacaoRepository, LocalService localService,
			UsuarioService usuarioService, ApplicationEventPublisher eventPublisher) {
		this.avaliacaoRepository = avaliacaoRepository;
		this.localService = localService;
		this.usuarioService = usuarioService;
		this.eventPublisher = eventPublisher;
	}

	@Transactional(readOnly = true)
	public PageResponse<AvaliacaoResponse> listar(Long localId, Pageable pageable) {
		if (localId != null) {
			localService.buscarEntidade(localId);
			return PageResponse.de(avaliacaoRepository.findByLocalId(localId, pageable).map(AvaliacaoResponse::de));
		}
		return PageResponse.de(avaliacaoRepository.findAll(pageable).map(AvaliacaoResponse::de));
	}

	@Transactional(readOnly = true)
	public PageResponse<AvaliacaoResponse> listarDoUsuario(Long usuarioId, Pageable pageable) {
		return PageResponse.de(avaliacaoRepository.findByUsuarioId(usuarioId, pageable).map(AvaliacaoResponse::de));
	}

	@Transactional(readOnly = true)
	public AvaliacaoResponse buscarPorId(Long id) {
		return AvaliacaoResponse.de(buscarEntidade(id));
	}

	@Transactional
	public AvaliacaoResponse criar(AvaliacaoRequest req, UsuarioAutenticado autor) {
		Usuario usuario = usuarioService.buscarEntidade(autor.id());
		Local local = localService.buscarEntidade(req.localId());
		if (avaliacaoRepository.existsByUsuarioIdAndLocalId(usuario.getId(), local.getId())) {
			throw new ConflitoException(
					"Você já avaliou este local. Use PUT /api/avaliacoes/{id} para atualizar sua avaliação");
		}
		Avaliacao avaliacao = new Avaliacao();
		avaliacao.setUsuario(usuario);
		avaliacao.setLocal(local);
		avaliacao.setNivelRuido(req.nivelRuido());
		avaliacao.setNivelIluminacao(req.nivelIluminacao());
		avaliacao.setNivelLotacao(req.nivelLotacao());
		avaliacao.setPeriodoVisita(req.periodoVisita());
		avaliacao.setComentario(textoOpcional(req.comentario()));
		avaliacaoRepository.saveAndFlush(avaliacao);

		eventPublisher.publishEvent(AvaliacaoAlteradaEvent.de(avaliacao, EnumTipoAlteracao.CRIADA));
		return AvaliacaoResponse.de(avaliacao);
	}

	@Transactional
	public AvaliacaoResponse atualizar(Long id, AvaliacaoUpdateRequest req, UsuarioAutenticado autor) {
		Avaliacao avaliacao = buscarEntidade(id);
		if (!avaliacao.getUsuario().getId().equals(autor.id())) {
			throw new AccessDeniedException("Somente o autor pode editar esta avaliação");
		}
		avaliacao.setNivelRuido(req.nivelRuido());
		avaliacao.setNivelIluminacao(req.nivelIluminacao());
		avaliacao.setNivelLotacao(req.nivelLotacao());
		avaliacao.setPeriodoVisita(req.periodoVisita());
		avaliacao.setComentario(textoOpcional(req.comentario()));
		avaliacaoRepository.saveAndFlush(avaliacao);

		eventPublisher.publishEvent(AvaliacaoAlteradaEvent.de(avaliacao, EnumTipoAlteracao.ATUALIZADA));
		return AvaliacaoResponse.de(avaliacao);
	}

	@Transactional
	public void remover(Long id, UsuarioAutenticado autor) {
		Avaliacao avaliacao = buscarEntidade(id);
		boolean dono = avaliacao.getUsuario().getId().equals(autor.id());
		if (!dono && !autor.isGestor()) {
			throw new AccessDeniedException("Somente o autor ou um gestor podem remover esta avaliação");
		}
		AvaliacaoAlteradaEvent evento = AvaliacaoAlteradaEvent.de(avaliacao, EnumTipoAlteracao.REMOVIDA);
		avaliacaoRepository.delete(avaliacao);
		eventPublisher.publishEvent(evento);
	}

	private Avaliacao buscarEntidade(Long id) {
		return avaliacaoRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Avaliação " + id + " não encontrada"));
	}

	private String textoOpcional(String texto) {
		return texto == null || texto.isBlank() ? null : texto.trim();
	}

}
