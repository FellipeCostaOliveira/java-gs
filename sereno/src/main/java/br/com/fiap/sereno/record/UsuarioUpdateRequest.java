package br.com.fiap.sereno.record;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateRequest(
		@NotBlank(message = "O nome é obrigatório")
		@Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
		String nome) {

}
