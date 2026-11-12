package br.com.fiap.sereno.security;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
public class JwtConfig {

	private static final Logger log = LoggerFactory.getLogger(JwtConfig.class);

	public static final String CLAIM_EMAIL = "email";
	public static final String CLAIM_ROLE = "role";
	private static final Set<String> ROLES_VALIDAS = Set.of("CIDADAO", "GESTOR");

	@Bean
	public SecretKey jwtSecretKey(JwtProperties props) {
		String segredo = props.secret();
		if (segredo == null || segredo.isBlank()) {
			byte[] aleatorio = new byte[64];
			new SecureRandom().nextBytes(aleatorio);
			log.warn("JWT_SECRET não definido: usando segredo aleatório desta execução. "
					+ "Defina JWT_SECRET (32+ caracteres) para integrar com a API .NET.");
			return new SecretKeySpec(aleatorio, "HmacSHA256");
		}
		byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < 32) {
			throw new IllegalStateException("JWT_SECRET deve ter pelo menos 32 caracteres (256 bits) para HS256");
		}
		return new SecretKeySpec(bytes, "HmacSHA256");
	}

	@Bean
	public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
	}

	@Bean
	public JwtDecoder jwtDecoder(SecretKey jwtSecretKey, JwtProperties props) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey).macAlgorithm(MacAlgorithm.HS256)
				.build();
		decoder.setJwtValidator(validador(props));
		return decoder;
	}

	public static OAuth2TokenValidator<Jwt> validador(JwtProperties props) {
		OAuth2TokenValidator<Jwt> emissor = JwtValidators.createDefaultWithIssuer(props.issuer());
		OAuth2TokenValidator<Jwt> audiencia = new JwtClaimValidator<List<String>>(JwtClaimNames.AUD,
				aud -> aud != null && aud.contains(props.audience()));
		OAuth2TokenValidator<Jwt> role = new JwtClaimValidator<String>(CLAIM_ROLE,
				r -> r != null && ROLES_VALIDAS.contains(r));
		return new DelegatingOAuth2TokenValidator<>(emissor, audiencia, role);
	}

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
		authorities.setAuthoritiesClaimName(CLAIM_ROLE);
		authorities.setAuthorityPrefix("ROLE_");
		JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
		conversor.setJwtGrantedAuthoritiesConverter(authorities);
		return conversor;
	}

}
