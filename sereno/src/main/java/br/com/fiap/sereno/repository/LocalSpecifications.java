package br.com.fiap.sereno.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import br.com.fiap.sereno.model.EnumCategoria;
import br.com.fiap.sereno.model.Local;
import jakarta.persistence.criteria.Predicate;

public final class LocalSpecifications {

	private LocalSpecifications() {

	}

	public static Specification<Local> comFiltros(String nome, EnumCategoria categoria, BigDecimal indiceMinimo) {
		return (root, query, cb) -> {
			List<Predicate> filtros = new ArrayList<>();
			if (nome != null && !nome.isBlank()) {
				String termo = "%" + nome.trim().toLowerCase(Locale.ROOT) + "%";
				filtros.add(cb.like(cb.lower(root.<String>get("nome")), termo));
			}
			if (categoria != null) {
				filtros.add(cb.equal(root.get("categoria"), categoria));
			}
			if (indiceMinimo != null) {
				filtros.add(cb.greaterThanOrEqualTo(root.<BigDecimal>get("indiceCalma"), indiceMinimo));
			}
			return cb.and(filtros.toArray(new Predicate[0]));
		};
	}

}
