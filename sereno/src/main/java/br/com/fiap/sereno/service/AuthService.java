package br.com.fiap.sereno.service;

import java.util.Locale;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.sereno.record.LoginRequest;
import br.com.fiap.sereno.record.RegisterRequest;
import br.com.fiap.sereno.record.TokenResponse;
import br.com.fiap.sereno.exception.ConflitoException;
import br.com.fiap.sereno.model.EnumRole;
import br.com.fiap.sereno.model.Usuario;
import br.com.fiap.sereno.repository.UsuarioRepository;
import br.com.fiap.sereno.security.TokenService;

@Service
public class AuthService {

	private static final String HASH_FALSO = "$2b$10$6X4H7JWMioXqkYn.LpZAtOwRe7Zhbo1xZlz0YrL9N90YnxDvDn0Dq";

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final TokenService tokenService;

	public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
			TokenService tokenService) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.tokenService = tokenService;
	}

	@Transactional
	public TokenResponse registrar(RegisterRequest req) {
		String email = normalizarEmail(req.email());
		if (usuarioRepository.existsByEmailIgnoreCase(email)) {
			throw new ConflitoException("Já existe uma conta cadastrada com este e-mail");
		}
		Usuario usuario = new Usuario(req.nome().trim(), email, passwordEncoder.encode(req.senha()),
				EnumRole.CIDADAO);
		usuarioRepository.saveAndFlush(usuario);
		return tokenService.gerarToken(usuario);
	}

	@Transactional(readOnly = true)
	public TokenResponse login(LoginRequest req) {
		Usuario usuario = usuarioRepository.findByEmailIgnoreCase(normalizarEmail(req.email())).orElse(null);
		if (usuario == null) {
			passwordEncoder.matches(req.senha(), HASH_FALSO);
			throw new BadCredentialsException("E-mail ou senha inválidos");
		}
		if (!passwordEncoder.matches(req.senha(), usuario.getSenhaHash())) {
			throw new BadCredentialsException("E-mail ou senha inválidos");
		}
		return tokenService.gerarToken(usuario);
	}

	private String normalizarEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

}
