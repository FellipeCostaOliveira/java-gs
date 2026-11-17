package br.com.fiap.sereno.control;

import java.math.BigDecimal;
import java.net.URI;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
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

import br.com.fiap.sereno.record.InsightResponse;
import br.com.fiap.sereno.record.LocalRequest;
import br.com.fiap.sereno.record.LocalResponse;
import br.com.fiap.sereno.record.PageResponse;
import br.com.fiap.sereno.model.EnumCategoria;
import br.com.fiap.sereno.security.UsuarioAutenticado;
import br.com.fiap.sereno.service.InsightService;
import br.com.fiap.sereno.service.LocalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

@RestController
@RequestMapping("/api/locais")
@Tag(name = "Locais", description = "Mapa sensorial: cadastro por CIDADAO e GESTOR, edição e exclusão só GESTOR, insight com IA")
public class LocalController {

	private final LocalService localService;
	private final InsightService insightService;

	public LocalController(LocalService localService, InsightService insightService) {
		this.localService = localService;
		this.insightService = insightService;
	}

	@GetMapping
	@Operation(summary = "Lista locais com filtros opcionais (paginado)")
	public PageResponse<LocalResponse> listar(
			@Parameter(description = "Parte do nome") @RequestParam(required = false) String nome,
			@RequestParam(required = false) EnumCategoria categoria,
			@Parameter(description = "Índice de calma mínimo (0 a 10)") @RequestParam(required = false)
			@DecimalMin(value = "0", message = "indiceMinimo deve estar entre 0 e 10")
			@DecimalMax(value = "10", message = "indiceMinimo deve estar entre 0 e 10") BigDecimal indiceMinimo,
			@ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
		return localService.listar(nome, categoria, indiceMinimo, pageable);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Detalhe de um local")
	public LocalResponse buscar(@PathVariable Long id) {
		return localService.buscarPorId(id);
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('CIDADAO','GESTOR')")
	@Operation(summary = "Cadastra um local (CIDADAO ou GESTOR)")
	public ResponseEntity<LocalResponse> criar(@RequestBody @Valid LocalRequest req,
			@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
		LocalResponse criado = localService.criar(req, UsuarioAutenticado.de(jwt).id());
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.id()).toUri();
		return ResponseEntity.created(uri).body(criado);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('GESTOR')")
	@Operation(summary = "Atualiza um local (GESTOR)")
	public LocalResponse atualizar(@PathVariable Long id, @RequestBody @Valid LocalRequest req) {
		return localService.atualizar(id, req);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('GESTOR')")
	@Operation(summary = "Remove um local e suas avaliações (GESTOR)")
	public ResponseEntity<Void> remover(@PathVariable Long id) {
		localService.remover(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}/insight")
	@Operation(summary = "Insight sensorial gerado por IA (Spring AI) a partir das avaliações do local")
	public InsightResponse insight(@PathVariable Long id) {
		return insightService.gerar(id);
	}

}