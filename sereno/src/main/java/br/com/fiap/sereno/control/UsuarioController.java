package br.com.fiap.sereno.control;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.sereno.record.AvaliacaoResponse;
import br.com.fiap.sereno.record.PageResponse;
import br.com.fiap.sereno.record.RoleUpdateRequest;
import br.com.fiap.sereno.record.UsuarioResponse;
import br.com.fiap.sereno.record.UsuarioUpdateRequest;
import br.com.fiap.sereno.security.UsuarioAutenticado;
import br.com.fiap.sereno.service.AvaliacaoService;
import br.com.fiap.sereno.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "Perfil do usuário logado e administração (GESTOR)")
public class UsuarioController {

	private final UsuarioService usuarioService;
	private final AvaliacaoService avaliacaoService;

	public UsuarioController(UsuarioService usuarioService, AvaliacaoService avaliacaoService) {
		this.usuarioService = usuarioService;
		this.avaliacaoService = avaliacaoService;
	}

	@GetMapping("/me")
	@Operation(summary = "Dados do usuário logado")
	public UsuarioResponse me(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
		return usuarioService.buscarPorId(UsuarioAutenticado.de(jwt).id());
	}

	@PutMapping("/me")
	@Operation(summary = "Atualiza o nome do usuário logado")
	public UsuarioResponse atualizarMe(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
			@RequestBody @Valid UsuarioUpdateRequest req) {
		return usuarioService.atualizarPerfil(UsuarioAutenticado.de(jwt).id(), req);
	}

	@GetMapping("/me/avaliacoes")
	@Operation(summary = "Avaliações feitas pelo usuário logado (paginado)")
	public PageResponse<AvaliacaoResponse> minhasAvaliacoes(
			@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
			@ParameterObject @PageableDefault(size = 10, sort = "atualizadoEm", direction = Sort.Direction.DESC) Pageable pageable) {
		return avaliacaoService.listarDoUsuario(UsuarioAutenticado.de(jwt).id(), pageable);
	}

	@GetMapping
	@PreAuthorize("hasRole('GESTOR')")
	@Operation(summary = "Lista usuários (GESTOR, paginado)")
	public PageResponse<UsuarioResponse> listar(
			@ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
		return usuarioService.listar(pageable);
	}

	@PatchMapping("/{id}/role")
	@PreAuthorize("hasRole('GESTOR')")
	@Operation(summary = "Altera o papel de um usuário (GESTOR)")
	public UsuarioResponse alterarRole(@PathVariable Long id, @RequestBody @Valid RoleUpdateRequest req,
			@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
		return usuarioService.alterarRole(id, req, UsuarioAutenticado.de(jwt).id());
	}

}
