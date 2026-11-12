package br.com.fiap.sereno.record;

import br.com.fiap.sereno.model.EnumPeriodo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AvaliacaoUpdateRequest(
		@NotNull(message = "O nível de ruído é obrigatório")
		@Min(value = 1, message = "O nível de ruído deve estar entre 1 e 5")
		@Max(value = 5, message = "O nível de ruído deve estar entre 1 e 5")
		Integer nivelRuido,

		@NotNull(message = "O nível de iluminação é obrigatório")
		@Min(value = 1, message = "O nível de iluminação deve estar entre 1 e 5")
		@Max(value = 5, message = "O nível de iluminação deve estar entre 1 e 5")
		Integer nivelIluminacao,

		@NotNull(message = "O nível de lotação é obrigatório")
		@Min(value = 1, message = "O nível de lotação deve estar entre 1 e 5")
		@Max(value = 5, message = "O nível de lotação deve estar entre 1 e 5")
		Integer nivelLotacao,

		@NotNull(message = "O período da visita é obrigatório (MANHA, TARDE ou NOITE)")
		EnumPeriodo periodoVisita,

		@Size(max = 500, message = "O comentário deve ter no máximo 500 caracteres")
		String comentario) {

}
