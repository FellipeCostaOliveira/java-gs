package br.com.fiap.sereno.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.fiap.sereno.record.EstatisticasAvaliacao;
import br.com.fiap.sereno.record.EstatisticasPeriodo;
import br.com.fiap.sereno.model.Avaliacao;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

	@EntityGraph(attributePaths = { "usuario", "local" })
	Page<Avaliacao> findByLocalId(Long localId, Pageable pageable);

	@EntityGraph(attributePaths = { "usuario", "local" })
	Page<Avaliacao> findByUsuarioId(Long usuarioId, Pageable pageable);

	@Override
	@EntityGraph(attributePaths = { "usuario", "local" })
	Page<Avaliacao> findAll(Pageable pageable);

	boolean existsByUsuarioIdAndLocalId(Long usuarioId, Long localId);

	List<Avaliacao> findTop20ByLocalIdAndComentarioIsNotNullOrderByAtualizadoEmDesc(Long localId);

	@Query("""
			select new br.com.fiap.sereno.record.EstatisticasAvaliacao(
			    count(a), avg(a.nivelRuido), avg(a.nivelIluminacao), avg(a.nivelLotacao))
			from Avaliacao a
			where a.local.id = :localId
			""")
	EstatisticasAvaliacao calcularEstatisticas(@Param("localId") Long localId);

	@Query("""
			select new br.com.fiap.sereno.record.EstatisticasPeriodo(
			    a.periodoVisita, count(a), avg(a.nivelRuido), avg(a.nivelIluminacao), avg(a.nivelLotacao))
			from Avaliacao a
			where a.local.id = :localId
			group by a.periodoVisita
			""")
	List<EstatisticasPeriodo> calcularEstatisticasPorPeriodo(@Param("localId") Long localId);

	@Modifying
	@Query("delete from Avaliacao a where a.local.id = :localId")
	int removerPorLocal(@Param("localId") Long localId);

}
