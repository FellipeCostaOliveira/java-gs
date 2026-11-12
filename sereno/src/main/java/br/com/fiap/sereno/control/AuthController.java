package br.com.fiap.sereno.control;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.sereno.record.LoginRequest;
import br.com.fiap.sereno.record.RegisterRequest;
import br.com.fiap.sereno.record.TokenResponse;
import br.com.fiap.sereno.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Cadastro e login (gera o JWT)")
@SecurityRequirements
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Cadastra um cidadão e já devolve o token")
	public TokenResponse registrar(@RequestBody @Valid RegisterRequest req) {
		return authService.registrar(req);
	}

	@PostMapping("/login")
	@Operation(summary = "Autentica com e-mail e senha e devolve o JWT")
	public TokenResponse login(@RequestBody @Valid LoginRequest req) {
		return authService.login(req);
	}

}
