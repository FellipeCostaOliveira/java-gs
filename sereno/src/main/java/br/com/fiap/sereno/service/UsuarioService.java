package br.com.fiap.sereno.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.sereno.record.PageResponse;
import br.com.fiap.sereno.record.RoleUpdateRequest;
import br.com.fiap.sereno.record.UsuarioResponse;
import br.com.fiap.sereno.record.UsuarioUpdateRequest;
import br.com.fiap.sereno.exception.RecursoNaoEncontradoException;
import br.com.fiap.sereno.exception.RegraNegocioException;
import br.com.fiap.sereno.model.Usuario;
import br.com.fiap.sereno.repository.UsuarioRepository;

@Service
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;

	public UsuarioService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Transactional(readOnly = true)
	public UsuarioResponse buscarPorId(Long id) {
		return UsuarioResponse.de(buscarEntidade(id));
	}

	@Transactional
	public UsuarioResponse atualizarPerfil(Long id, UsuarioUpdateRequest req) {
		Usuario usuario = buscarEntidade(id);
		usuario.setNome(req.nome().trim());
		usuarioRepository.saveAndFlush(usuario);
		return UsuarioResponse.de(usuario);
	}

	@Transactional(readOnly = true)
	public PageResponse<UsuarioResponse> listar(Pageable pageable) {
		return PageResponse.de(usuarioRepository.findAll(pageable).map(UsuarioResponse::de));
	}

	@Transactional
	public UsuarioResponse alterarRole(Long id, RoleUpdateRequest req, Long gestorId) {
		if (id.equals(gestorId)) {
			throw new RegraNegocioException("Um gestor não pode alterar o próprio papel");
		}
		Usuario usuario = buscarEntidade(id);
		usuario.setRole(req.role());
		usuarioRepository.saveAndFlush(usuario);
		return UsuarioResponse.de(usuario);
	}

	public Usuario buscarEntidade(Long id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Usuário " + id + " não encontrado"));
	}

}
