package br.com.fiap.sereno.security;

import org.springframework.security.oauth2.jwt.Jwt;

import br.com.fiap.sereno.model.EnumRole;

public record UsuarioAutenticado(Long id, String email, EnumRole role) {

	public static UsuarioAutenticado de(Jwt jwt) {
		return new UsuarioAutenticado(Long.valueOf(jwt.getSubject()), jwt.getClaimAsString(JwtConfig.CLAIM_EMAIL),
				EnumRole.valueOf(jwt.getClaimAsString(JwtConfig.CLAIM_ROLE)));
	}

	public boolean isGestor() {
		return role == EnumRole.GESTOR;
	}

}
