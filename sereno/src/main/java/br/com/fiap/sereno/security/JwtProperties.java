package br.com.fiap.sereno.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "sereno.jwt")
public record JwtProperties(
		String secret,

		@NotBlank String issuer,

		@NotBlank String audience,

		@Min(1) long expiracaoMinutos) {

}
