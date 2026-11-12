package br.com.fiap.sereno.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import br.com.fiap.sereno.record.TokenResponse;
import br.com.fiap.sereno.model.EnumRole;
import br.com.fiap.sereno.model.Usuario;

class TokenServiceTest {

	private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-bytes-123456";

	private final JwtConfig config = new JwtConfig();

	private TokenResponse gerar(JwtProperties props) {
		SecretKey chave = config.jwtSecretKey(props);
		TokenService service = new TokenService(config.jwtEncoder(chave), props);
		Usuario usuario = new Usuario("Gestor", "gestor@sereno.app", "hash", EnumRole.GESTOR);
		usuario.setId(42L);
		return service.gerarToken(usuario);
	}

	@Test
	void tokenGeradoContemClaimsEsperadasEValida() {
		JwtProperties props = new JwtProperties(SEGREDO, "sereno-auth", "sereno-app", 60);
		TokenResponse resposta = gerar(props);

		JwtDecoder decoder = config.jwtDecoder(config.jwtSecretKey(props), props);
		Jwt jwt = decoder.decode(resposta.token());

		assertEquals("42", jwt.getSubject());
		assertEquals("gestor@sereno.app", jwt.getClaimAsString("email"));
		assertEquals("GESTOR", jwt.getClaimAsString("role"));
		assertEquals("sereno-auth", jwt.getClaimAsString("iss"));
		assertTrue(jwt.getAudience().contains("sereno-app"));
		assertEquals("HS256", String.valueOf(jwt.getHeaders().get("alg")));
		assertEquals(3600, resposta.expiraEmSegundos());
	}

	@Test
	void semJwtSecretUsaSegredoAleatorioQueFunciona() {
		JwtProperties props = new JwtProperties("", "sereno-auth", "sereno-app", 60);
		SecretKey chave = config.jwtSecretKey(props);
		TokenService service = new TokenService(config.jwtEncoder(chave), props);
		Usuario usuario = new Usuario("Ana", "ana@sereno.app", "hash", EnumRole.CIDADAO);
		usuario.setId(7L);

		Jwt jwt = config.jwtDecoder(chave, props).decode(service.gerarToken(usuario).token());

		assertEquals("7", jwt.getSubject());
		assertEquals("CIDADAO", jwt.getClaimAsString("role"));
	}

	@Test
	void segredoCurtoEhRecusado() {
		JwtProperties props = new JwtProperties("curto", "sereno-auth", "sereno-app", 60);
		assertThrows(IllegalStateException.class, () -> config.jwtSecretKey(props));
	}

	@Test
	void tokenComAudienciaDiferenteEhRejeitado() {
		JwtProperties emissor = new JwtProperties(SEGREDO, "sereno-auth", "outra-app", 60);
		TokenResponse resposta = gerar(emissor);

		JwtProperties api = new JwtProperties(SEGREDO, "sereno-auth", "sereno-app", 60);
		JwtDecoder decoder = config.jwtDecoder(config.jwtSecretKey(api), api);

		assertThrows(JwtException.class, () -> decoder.decode(resposta.token()));
	}

	@Test
	void tokenAssinadoComOutroSegredoEhRejeitado() {
		JwtProperties emissor = new JwtProperties("outro-segredo-qualquer-com-mais-de-32-bytes!!", "sereno-auth",
				"sereno-app", 60);
		TokenResponse resposta = gerar(emissor);

		JwtProperties api = new JwtProperties(SEGREDO, "sereno-auth", "sereno-app", 60);
		JwtDecoder decoder = config.jwtDecoder(config.jwtSecretKey(api), api);

		assertThrows(JwtException.class, () -> decoder.decode(resposta.token()));
	}

}
