package br.com.fiap.sereno.record;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@Schema(example = "cidadao@sereno.app")
		@NotBlank(message = "O e-mail é obrigatório")
		@Email(message = "E-mail inválido")
		String email,

		@Schema(example = "Sereno@123")
		@NotBlank(message = "A senha é obrigatória")
		String senha) {

}
