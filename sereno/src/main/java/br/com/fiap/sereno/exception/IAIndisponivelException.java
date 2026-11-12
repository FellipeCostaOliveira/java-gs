package br.com.fiap.sereno.exception;

public class IAIndisponivelException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public IAIndisponivelException(String mensagem) {
		super(mensagem);
	}

	public IAIndisponivelException(String mensagem, Throwable causa) {
		super(mensagem, causa);
	}

}
