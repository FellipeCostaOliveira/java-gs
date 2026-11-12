package br.com.fiap.sereno.record;

public record TokenResponse(String token, String tipo, long expiraEmSegundos, UsuarioResponse usuario) {

}
