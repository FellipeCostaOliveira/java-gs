package br.com.fiap.sereno.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.sereno.record.LocalRequest;
import br.com.fiap.sereno.record.LocalResponse;
import br.com.fiap.sereno.record.PageResponse;
import br.com.fiap.sereno.exception.RecursoNaoEncontradoException;
import br.com.fiap.sereno.model.EnumCategoria;
import br.com.fiap.sereno.model.Local;
import br.com.fiap.sereno.repository.AvaliacaoRepository;
import br.com.fiap.sereno.repository.LocalRepository;
import br.com.fiap.sereno.repository.LocalSpecifications;
import br.com.fiap.sereno.repository.UsuarioRepository;

@Service
public class LocalService {

	private final LocalRepository localRepository;
	private final AvaliacaoRepository avaliacaoRepository;
	private final UsuarioRepository usuarioRepository;
	private final InsightService insightService;

	public LocalService(LocalRepository localRepository, AvaliacaoRepository avaliacaoRepository,
			UsuarioRepository usuarioRepository, InsightService insightService) {
		this.localRepository = localRepository;
		this.avaliacaoRepository = avaliacaoRepository;
		this.usuarioRepository = usuarioRepository;
		this.insightService = insightService;
	}

	@Transactional(readOnly = true)
	public PageResponse<LocalResponse> listar(String nome, EnumCategoria categoria, BigDecimal indiceMinimo,
			Pageable pageable) {
		return PageResponse.de(localRepository
				.findAll(LocalSpecifications.comFiltros(nome, categoria, indiceMinimo), pageable)
				.map(LocalResponse::de));
	}

	@Transactional(readOnly = true)
	public LocalResponse buscarPorId(Long id) {
		return LocalResponse.de(buscarEntidade(id));
	}

	@Transactional
	public LocalResponse criar(LocalRequest req, Long gestorId) {
		Local local = new Local();
		preencher(local, req);
		local.setTotalAvaliacoes(0);
		local.setCriadoPor(usuarioRepository.getReferenceById(gestorId));
		localRepository.saveAndFlush(local);
		return LocalResponse.de(local);
	}

	@Transactional
	public LocalResponse atualizar(Long id, LocalRequest req) {
		Local local = buscarEntidade(id);
		preencher(local, req);
		localRepository.saveAndFlush(local);
		insightService.invalidarCache(id);
		return LocalResponse.de(local);
	}

	@Transactional
	public void remover(Long id) {
		Local local = buscarEntidade(id);
		avaliacaoRepository.removerPorLocal(id);
		localRepository.delete(local);
		insightService.invalidarCache(id);
	}

	private void preencher(Local local, LocalRequest req) {
		local.setNome(req.nome().trim());
		local.setDescricao(textoOpcional(req.descricao()));
		local.setCategoria(req.categoria());
		local.setEndereco(req.endereco().trim());
		local.setLatitude(req.latitude().setScale(6, RoundingMode.HALF_UP));
		local.setLongitude(req.longitude().setScale(6, RoundingMode.HALF_UP));
		local.setRecursosAcessibilidade(textoOpcional(req.recursosAcessibilidade()));
	}

	private String textoOpcional(String texto) {
		return texto == null || texto.isBlank() ? null : texto.trim();
	}

	public Local buscarEntidade(Long id) {
		return localRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Local " + id + " não encontrado"));
	}

}
