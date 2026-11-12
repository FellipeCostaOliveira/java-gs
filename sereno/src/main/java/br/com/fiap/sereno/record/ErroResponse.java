package br.com.fiap.sereno.record;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;

public record ErroResponse(int status, String erro, String mensagem, List<CampoErro> campos,
		OffsetDateTime timestamp, String path) {

	public static ErroResponse de(HttpStatus status, String mensagem, List<CampoErro> campos, String path) {
		return new ErroResponse(status.value(), status.getReasonPhrase(), mensagem,
				campos == null ? List.of() : campos, OffsetDateTime.now(), path);
	}

	public static ErroResponse de(HttpStatus status, String mensagem, String path) {
		return de(status, mensagem, List.of(), path);
	}

}
