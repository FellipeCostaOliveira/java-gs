package br.com.fiap.sereno.record;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@Schema(example = "Maria Silva")
		@NotBlank(message = "O nome é obrigatório")
		@Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
		String nome,

		@Schema(example = "maria@email.com")
		@NotBlank(message = "O e-mail é obrigatório")
		@Email(message = "E-mail inválido")
		@Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
		String email,

		@Schema(example = "Senha@123")
		@NotBlank(message = "A senha é obrigatória")
		@Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
		@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "A senha deve conter letras e números")
		String senha) {

}
