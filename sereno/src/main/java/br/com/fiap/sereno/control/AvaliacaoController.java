package br.com.fiap.sereno.control;

import java.net.URI;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.fiap.sereno.record.AvaliacaoRequest;
import br.com.fiap.sereno.record.AvaliacaoResponse;
import br.com.fiap.sereno.record.AvaliacaoUpdateRequest;
import br.com.fiap.sereno.record.PageResponse;
import br.com.fiap.sereno.security.UsuarioAutenticado;
import br.com.fiap.sereno.service.AvaliacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/avaliacoes")
@Tag(name = "Avaliações", description = "Avaliações sensoriais (ruído, iluminação, lotação). Editar: só o dono")
public class AvaliacaoController {

	private final AvaliacaoService avaliacaoService;

	public AvaliacaoController(AvaliacaoService avaliacaoService) {
		this.avaliacaoService = avaliacaoService;
	}

	@GetMapping
	@Operation(summary = "Lista avaliações (filtro opcional por local, paginado)")
	public PageResponse<AvaliacaoResponse> listar(@RequestParam(required = false) Long localId,
			@ParameterObject @PageableDefault(size = 10, sort = "atualizadoEm", direction = Sort.Direction.DESC) Pageable pageable) {
		return avaliacaoService.listar(localId, pageable);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Detalhe de uma avaliação")
	public AvaliacaoResponse buscar(@PathVariable Long id) {
		return avaliacaoService.buscarPorId(id);
	}

	@PostMapping
	@PreAuthorize("hasRole('CIDADAO')")
	@Operation(summary = "Cria uma avaliação (CIDADAO). Dispara o evento AvaliacaoAlteradaEvent")
	public ResponseEntity<AvaliacaoResponse> criar(@RequestBody @Valid AvaliacaoRequest req,
			@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
		AvaliacaoResponse criada = avaliacaoService.criar(req, UsuarioAutenticado.de(jwt));
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criada.id()).toUri();
		return ResponseEntity.created(uri).body(criada);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('CIDADAO')")
	@Operation(summary = "Atualiza a própria avaliação (somente o dono)")
	public AvaliacaoResponse atualizar(@PathVariable Long id, @RequestBody @Valid AvaliacaoUpdateRequest req,
			@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
		return avaliacaoService.atualizar(id, req, UsuarioAutenticado.de(jwt));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Remove a avaliação (dono ou GESTOR para moderação)")
	public ResponseEntity<Void> remover(@PathVariable Long id,
			@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
		avaliacaoService.remover(id, UsuarioAutenticado.de(jwt));
		return ResponseEntity.noContent().build();
	}

}
