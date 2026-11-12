package br.com.fiap.sereno.security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.fiap.sereno.record.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ErroSegurancaHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	public ErroSegurancaHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		String mensagem = authException instanceof InvalidBearerTokenException
				? "Token inválido, expirado ou emitido para outra aplicação"
				: "Autenticação necessária: envie o header Authorization: Bearer <token>";
		response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
		escrever(response, ErroResponse.de(HttpStatus.UNAUTHORIZED, mensagem, request.getRequestURI()));
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		escrever(response, ErroResponse.de(HttpStatus.FORBIDDEN,
				"Seu papel não tem permissão para acessar este recurso", request.getRequestURI()));
	}

	private void escrever(HttpServletResponse response, ErroResponse erro) throws IOException {
		response.setStatus(erro.status());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getOutputStream(), erro);
	}

}
