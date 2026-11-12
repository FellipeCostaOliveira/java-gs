package br.com.fiap.sereno.record;

import br.com.fiap.sereno.model.EnumPeriodo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AvaliacaoRequest(
		@Schema(example = "1")
		@NotNull(message = "O id do local é obrigatório")
		@Positive(message = "O id do local deve ser positivo")
		Long localId,

		@Schema(description = "1 = silencioso ... 5 = muito barulhento", example = "2")
		@NotNull(message = "O nível de ruído é obrigatório")
		@Min(value = 1, message = "O nível de ruído deve estar entre 1 e 5")
		@Max(value = 5, message = "O nível de ruído deve estar entre 1 e 5")
		Integer nivelRuido,

		@Schema(description = "1 = luz suave ... 5 = luz muito intensa", example = "3")
		@NotNull(message = "O nível de iluminação é obrigatório")
		@Min(value = 1, message = "O nível de iluminação deve estar entre 1 e 5")
		@Max(value = 5, message = "O nível de iluminação deve estar entre 1 e 5")
		Integer nivelIluminacao,

		@Schema(description = "1 = vazio ... 5 = lotado", example = "2")
		@NotNull(message = "O nível de lotação é obrigatório")
		@Min(value = 1, message = "O nível de lotação deve estar entre 1 e 5")
		@Max(value = 5, message = "O nível de lotação deve estar entre 1 e 5")
		Integer nivelLotacao,

		@Schema(example = "MANHA")
		@NotNull(message = "O período da visita é obrigatório (MANHA, TARDE ou NOITE)")
		EnumPeriodo periodoVisita,

		@Schema(example = "Ambiente tranquilo pela manhã.")
		@Size(max = 500, message = "O comentário deve ter no máximo 500 caracteres")
		String comentario) {

}
