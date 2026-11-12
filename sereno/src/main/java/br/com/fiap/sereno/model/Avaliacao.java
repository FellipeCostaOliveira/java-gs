package br.com.fiap.sereno.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "SER_AVALIACAO", uniqueConstraints = @UniqueConstraint(name = "UK_SER_AVALIACAO_USUARIO_LOCAL", columnNames = {
		"USUARIO_ID", "LOCAL_ID" }))
public class Avaliacao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "USUARIO_ID", nullable = false)
	private Usuario usuario;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "LOCAL_ID", nullable = false)
	private Local local;

	@Column(name = "NIVEL_RUIDO", nullable = false)
	private Integer nivelRuido;

	@Column(name = "NIVEL_ILUMINACAO", nullable = false)
	private Integer nivelIluminacao;

	@Column(name = "NIVEL_LOTACAO", nullable = false)
	private Integer nivelLotacao;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "PERIODO_VISITA", nullable = false, length = 10)
	private EnumPeriodo periodoVisita;

	@Column(name = "COMENTARIO", length = 500)
	private String comentario;

	@CreationTimestamp
	@Column(name = "CRIADO_EM", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	@UpdateTimestamp
	@Column(name = "ATUALIZADO_EM", nullable = false)
	private LocalDateTime atualizadoEm;

	public Avaliacao() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Local getLocal() {
		return local;
	}

	public void setLocal(Local local) {
		this.local = local;
	}

	public Integer getNivelRuido() {
		return nivelRuido;
	}

	public void setNivelRuido(Integer nivelRuido) {
		this.nivelRuido = nivelRuido;
	}

	public Integer getNivelIluminacao() {
		return nivelIluminacao;
	}

	public void setNivelIluminacao(Integer nivelIluminacao) {
		this.nivelIluminacao = nivelIluminacao;
	}

	public Integer getNivelLotacao() {
		return nivelLotacao;
	}

	public void setNivelLotacao(Integer nivelLotacao) {
		this.nivelLotacao = nivelLotacao;
	}

	public EnumPeriodo getPeriodoVisita() {
		return periodoVisita;
	}

	public void setPeriodoVisita(EnumPeriodo periodoVisita) {
		this.periodoVisita = periodoVisita;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public LocalDateTime getAtualizadoEm() {
		return atualizadoEm;
	}

}
