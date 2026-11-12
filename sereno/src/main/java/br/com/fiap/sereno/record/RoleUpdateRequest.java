package br.com.fiap.sereno.record;

import br.com.fiap.sereno.model.EnumRole;
import jakarta.validation.constraints.NotNull;

public record RoleUpdateRequest(
		@NotNull(message = "O papel é obrigatório (CIDADAO ou GESTOR)")
		EnumRole role) {

}
