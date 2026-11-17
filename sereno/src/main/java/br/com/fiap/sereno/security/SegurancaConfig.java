package br.com.fiap.sereno.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SegurancaConfig {

	private static final String[] ROTAS_PUBLICAS = { "/", "/login", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
			"/actuator/health", "/actuator/health/**", "/actuator/info", "/error" };

	private static final String CIDADAO = "CIDADAO";
	private static final String GESTOR = "GESTOR";

	@Bean
	public SecurityFilterChain filtrar(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter,
			ErroSegurancaHandler erroHandler) throws Exception {

		http.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(req -> req
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
						.requestMatchers(ROTAS_PUBLICAS).permitAll()

						.requestMatchers(HttpMethod.POST, "/api/locais").hasAnyRole(CIDADAO, GESTOR)
						.requestMatchers(HttpMethod.PUT, "/api/locais/*").hasRole(GESTOR)
						.requestMatchers(HttpMethod.DELETE, "/api/locais/*").hasRole(GESTOR)

						.requestMatchers(HttpMethod.GET, "/api/usuarios").hasRole(GESTOR)
						.requestMatchers(HttpMethod.PATCH, "/api/usuarios/*/role").hasRole(GESTOR)

						.requestMatchers(HttpMethod.POST, "/api/avaliacoes").hasRole(CIDADAO)
						.requestMatchers(HttpMethod.PUT, "/api/avaliacoes/*").hasRole(CIDADAO)

						.requestMatchers("/api/**").hasAnyRole(CIDADAO, GESTOR)
						.anyRequest().denyAll())
				.oauth2ResourceServer(oauth -> oauth
						.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
						.authenticationEntryPoint(erroHandler)
						.accessDeniedHandler(erroHandler))
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(erroHandler)
						.accessDeniedHandler(erroHandler));

		return http.build();
	}

	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource(
			@Value("${sereno.cors.allowed-origins:*}") List<String> origens) {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOriginPatterns(origens);
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
		config.setExposedHeaders(List.of("Location"));
		config.setAllowCredentials(false);
		config.setMaxAge(3600L);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", config);
		return source;
	}

}