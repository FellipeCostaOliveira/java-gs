package br.com.fiap.sereno.record;

import java.math.BigDecimal;

import br.com.fiap.sereno.model.EnumCategoria;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LocalRequest(
		@Schema(example = "Biblioteca Municipal Centro")
		@NotBlank(message = "O nome é obrigatório")
		@Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
		String nome,

		@Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
		String descricao,

		@Schema(example = "BIBLIOTECA")
		@NotNull(message = "A categoria é obrigatória")
		EnumCategoria categoria,

		@Schema(example = "Rua Exemplo, 100 - Centro, São Paulo - SP")
		@NotBlank(message = "O endereço é obrigatório")
		@Size(max = 255, message = "O endereço deve ter no máximo 255 caracteres")
		String endereco,

		@Schema(example = "-23.550520")
		@NotNull(message = "A latitude é obrigatória")
		@DecimalMin(value = "-90.0", message = "A latitude deve estar entre -90 e 90")
		@DecimalMax(value = "90.0", message = "A latitude deve estar entre -90 e 90")
		BigDecimal latitude,

		@Schema(example = "-46.633308")
		@NotNull(message = "A longitude é obrigatória")
		@DecimalMin(value = "-180.0", message = "A longitude deve estar entre -180 e 180")
		@DecimalMax(value = "180.0", message = "A longitude deve estar entre -180 e 180")
		BigDecimal longitude,

		@Schema(example = "Rampa de acesso; sala silenciosa; piso tátil")
		@Size(max = 500, message = "Os recursos de acessibilidade devem ter no máximo 500 caracteres")
		String recursosAcessibilidade) {

}
