package br.com.fiap.sereno.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import br.com.fiap.sereno.record.TokenResponse;
import br.com.fiap.sereno.record.UsuarioResponse;
import br.com.fiap.sereno.model.Usuario;

@Service
public class TokenService {

	private final JwtEncoder jwtEncoder;
	private final JwtProperties props;

	public TokenService(JwtEncoder jwtEncoder, JwtProperties props) {
		this.jwtEncoder = jwtEncoder;
		this.props = props;
	}

	public TokenResponse gerarToken(Usuario usuario) {
		Instant agora = Instant.now();
		Instant expiracao = agora.plus(props.expiracaoMinutos(), ChronoUnit.MINUTES);

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.id(UUID.randomUUID().toString())
				.issuer(props.issuer())
				.audience(List.of(props.audience()))
				.subject(String.valueOf(usuario.getId()))
				.issuedAt(agora)
				.notBefore(agora)
				.expiresAt(expiracao)
				.claim(JwtConfig.CLAIM_EMAIL, usuario.getEmail())
				.claim(JwtConfig.CLAIM_ROLE, usuario.getRole().name())
				.build();

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
		String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

		return new TokenResponse(token, "Bearer", props.expiracaoMinutos() * 60, UsuarioResponse.de(usuario));
	}

}
