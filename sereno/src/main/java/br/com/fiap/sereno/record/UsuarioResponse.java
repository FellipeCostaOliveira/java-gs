package br.com.fiap.sereno.record;

import java.time.LocalDateTime;

import br.com.fiap.sereno.model.EnumRole;
import br.com.fiap.sereno.model.Usuario;

public record UsuarioResponse(Long id, String nome, String email, EnumRole role, LocalDateTime criadoEm) {

	public static UsuarioResponse de(Usuario usuario) {
		return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getRole(),
				usuario.getCriadoEm());
	}

}
