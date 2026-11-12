package br.com.fiap.sereno.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
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

@Entity
@DynamicUpdate
@Table(name = "SER_LOCAL")
public class Local {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "NOME", nullable = false, length = 120)
	private String nome;

	@Column(name = "DESCRICAO", length = 1000)
	private String descricao;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "CATEGORIA", nullable = false, length = 30)
	private EnumCategoria categoria;

	@Column(name = "ENDERECO", nullable = false, length = 255)
	private String endereco;

	@Column(name = "LATITUDE", nullable = false, precision = 9, scale = 6)
	private BigDecimal latitude;

	@Column(name = "LONGITUDE", nullable = false, precision = 9, scale = 6)
	private BigDecimal longitude;

	@Column(name = "RECURSOS_ACESSIBILIDADE", length = 500)
	private String recursosAcessibilidade;

	@Column(name = "INDICE_CALMA", precision = 4, scale = 2)
	private BigDecimal indiceCalma;

	@Column(name = "TOTAL_AVALIACOES", nullable = false)
	private Integer totalAvaliacoes = 0;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CRIADO_POR_ID")
	private Usuario criadoPor;

	@CreationTimestamp
	@Column(name = "CRIADO_EM", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	@UpdateTimestamp
	@Column(name = "ATUALIZADO_EM", nullable = false)
	private LocalDateTime atualizadoEm;

	public Local() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public EnumCategoria getCategoria() {
		return categoria;
	}

	public void setCategoria(EnumCategoria categoria) {
		this.categoria = categoria;
	}

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	public BigDecimal getLatitude() {
		return latitude;
	}

	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}

	public BigDecimal getLongitude() {
		return longitude;
	}

	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}

	public String getRecursosAcessibilidade() {
		return recursosAcessibilidade;
	}

	public void setRecursosAcessibilidade(String recursosAcessibilidade) {
		this.recursosAcessibilidade = recursosAcessibilidade;
	}

	public BigDecimal getIndiceCalma() {
		return indiceCalma;
	}

	public void setIndiceCalma(BigDecimal indiceCalma) {
		this.indiceCalma = indiceCalma;
	}

	public Integer getTotalAvaliacoes() {
		return totalAvaliacoes;
	}

	public void setTotalAvaliacoes(Integer totalAvaliacoes) {
		this.totalAvaliacoes = totalAvaliacoes;
	}

	public Usuario getCriadoPor() {
		return criadoPor;
	}

	public void setCriadoPor(Usuario criadoPor) {
		this.criadoPor = criadoPor;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public LocalDateTime getAtualizadoEm() {
		return atualizadoEm;
	}

}
