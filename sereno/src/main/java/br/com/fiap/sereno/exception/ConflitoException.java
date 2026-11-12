package br.com.fiap.sereno.exception;

public class ConflitoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConflitoException(String mensagem) {
		super(mensagem);
	}

	public ConflitoException(String mensagem, Throwable causa) {
		super(mensagem, causa);
	}

}
