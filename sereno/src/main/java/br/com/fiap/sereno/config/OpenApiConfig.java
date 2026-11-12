package br.com.fiap.sereno.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	public static final String BEARER = "bearerAuth";

	@Bean
	public OpenAPI serenoOpenApi() {
		return new OpenAPI()
				.info(new Info().title("Sereno API").version("1.0.0")
						.description("CitizenTech - acessibilidade urbana com mapa sensorial de lugares. "
								+ "Faça login em /api/auth/login, copie o token e clique em Authorize. "
								+ "Usuários de exemplo: gestor@sereno.app / cidadao@sereno.app (senha Sereno@123)."))
				.components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
						.type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
				.addSecurityItem(new SecurityRequirement().addList(BEARER));
	}

}
