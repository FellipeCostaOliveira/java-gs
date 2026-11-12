package br.com.fiap.sereno.exception;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import br.com.fiap.sereno.record.CampoErro;
import br.com.fiap.sereno.record.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
		List<CampoErro> campos = new ArrayList<>();
		for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
			campos.add(new CampoErro(erro.getField(), erro.getDefaultMessage()));
		}
		ex.getBindingResult().getGlobalErrors()
				.forEach(erro -> campos.add(new CampoErro(erro.getObjectName(), erro.getDefaultMessage())));
		return responder(HttpStatus.BAD_REQUEST, "Um ou mais campos são inválidos", campos, req);
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<ErroResponse> tratarValidacaoParametros(HandlerMethodValidationException ex,
			HttpServletRequest req) {
		List<CampoErro> campos = new ArrayList<>();
		ex.getParameterValidationResults().forEach(resultado -> resultado.getResolvableErrors()
				.forEach(erro -> campos.add(new CampoErro(resultado.getMethodParameter().getParameterName(),
						erro.getDefaultMessage()))));
		return responder(HttpStatus.BAD_REQUEST, "Um ou mais parâmetros são inválidos", campos, req);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErroResponse> tratarConstraint(ConstraintViolationException ex, HttpServletRequest req) {
		List<CampoErro> campos = ex.getConstraintViolations().stream()
				.map(v -> new CampoErro(v.getPropertyPath().toString(), v.getMessage())).toList();
		return responder(HttpStatus.BAD_REQUEST, "Um ou mais parâmetros são inválidos", campos, req);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErroResponse> tratarCorpoInvalido(HttpMessageNotReadableException ex,
			HttpServletRequest req) {
		return responder(HttpStatus.BAD_REQUEST,
				"Corpo da requisição ausente ou mal formatado (verifique o JSON e os valores dos enums)", List.of(),
				req);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErroResponse> tratarTipoInvalido(MethodArgumentTypeMismatchException ex,
			HttpServletRequest req) {
		return responder(HttpStatus.BAD_REQUEST, "Parâmetro com tipo inválido",
				List.of(new CampoErro(ex.getName(), "Valor '" + ex.getValue() + "' inválido")), req);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErroResponse> tratarParametroAusente(MissingServletRequestParameterException ex,
			HttpServletRequest req) {
		return responder(HttpStatus.BAD_REQUEST, "Parâmetro obrigatório ausente",
				List.of(new CampoErro(ex.getParameterName(), "Parâmetro obrigatório")), req);
	}

	@ExceptionHandler(PropertyReferenceException.class)
	public ResponseEntity<ErroResponse> tratarOrdenacaoInvalida(PropertyReferenceException ex,
			HttpServletRequest req) {
		return responder(HttpStatus.BAD_REQUEST, "Campo de ordenação inválido",
				List.of(new CampoErro("sort", "Propriedade '" + ex.getPropertyName() + "' não existe")), req);
	}

	@ExceptionHandler(InvalidDataAccessApiUsageException.class)
	public ResponseEntity<ErroResponse> tratarConsultaInvalida(InvalidDataAccessApiUsageException ex,
			HttpServletRequest req) {
		return responder(HttpStatus.BAD_REQUEST, "Parâmetros de consulta inválidos (verifique paginação e ordenação)",
				List.of(), req);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErroResponse> tratarCredenciais(BadCredentialsException ex, HttpServletRequest req) {
		return responder(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos", List.of(), req);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ErroResponse> tratarAutenticacao(AuthenticationException ex, HttpServletRequest req) {
		return responder(HttpStatus.UNAUTHORIZED, "Autenticação necessária", List.of(), req);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErroResponse> tratarAcessoNegado(AccessDeniedException ex, HttpServletRequest req) {
		String mensagem = ex.getMessage() != null && !ex.getMessage().startsWith("Access Denied")
				? ex.getMessage()
				: "Você não tem permissão para acessar este recurso";
		return responder(HttpStatus.FORBIDDEN, mensagem, List.of(), req);
	}

	@ExceptionHandler(RecursoNaoEncontradoException.class)
	public ResponseEntity<ErroResponse> tratarNaoEncontrado(RecursoNaoEncontradoException ex,
			HttpServletRequest req) {
		return responder(HttpStatus.NOT_FOUND, ex.getMessage(), List.of(), req);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErroResponse> tratarRotaInexistente(NoResourceFoundException ex, HttpServletRequest req) {
		return responder(HttpStatus.NOT_FOUND, "Rota não encontrada", List.of(), req);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErroResponse> tratarMetodo(HttpRequestMethodNotSupportedException ex,
			HttpServletRequest req) {
		return responder(HttpStatus.METHOD_NOT_ALLOWED, "Método " + ex.getMethod() + " não suportado nesta rota",
				List.of(), req);
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ErroResponse> tratarMediaType(HttpMediaTypeNotSupportedException ex,
			HttpServletRequest req) {
		return responder(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Use Content-Type: application/json", List.of(), req);
	}

	@ExceptionHandler(ConflitoException.class)
	public ResponseEntity<ErroResponse> tratarConflito(ConflitoException ex, HttpServletRequest req) {
		return responder(HttpStatus.CONFLICT, ex.getMessage(), List.of(), req);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErroResponse> tratarIntegridade(DataIntegrityViolationException ex,
			HttpServletRequest req) {
		log.warn("Violação de integridade em {}: {}", req.getRequestURI(), ex.getMostSpecificCause().getMessage());
		return responder(HttpStatus.CONFLICT, "Operação viola uma regra de integridade dos dados", List.of(), req);
	}

	@ExceptionHandler(RegraNegocioException.class)
	public ResponseEntity<ErroResponse> tratarRegraNegocio(RegraNegocioException ex, HttpServletRequest req) {
		return responder(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), List.of(), req);
	}

	@ExceptionHandler(ServletRequestBindingException.class)
	public ResponseEntity<ErroResponse> tratarBinding(ServletRequestBindingException ex, HttpServletRequest req) {
		return responder(HttpStatus.BAD_REQUEST, "Requisição incompleta: parâmetro ou cabeçalho obrigatório ausente",
				List.of(), req);
	}

	@ExceptionHandler(ErrorResponseException.class)
	public ResponseEntity<ErroResponse> tratarErrorResponse(ErrorResponseException ex, HttpServletRequest req) {
		HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
		if (status == null) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		String detalhe = ex.getBody().getDetail();
		return responder(status, detalhe != null ? detalhe : status.getReasonPhrase(), List.of(), req);
	}

	@ExceptionHandler(IAIndisponivelException.class)
	public ResponseEntity<ErroResponse> tratarIA(IAIndisponivelException ex, HttpServletRequest req) {
		return responder(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), List.of(), req);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErroResponse> tratarGenerico(Exception ex, HttpServletRequest req) {
		log.error("Erro inesperado em {} {}", req.getMethod(), req.getRequestURI(), ex);
		return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado. Tente novamente mais tarde.",
				List.of(), req);
	}

	private ResponseEntity<ErroResponse> responder(HttpStatus status, String mensagem, List<CampoErro> campos,
			HttpServletRequest req) {
		return ResponseEntity.status(status).body(ErroResponse.de(status, mensagem, campos, req.getRequestURI()));
	}

}
