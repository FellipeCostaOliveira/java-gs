package br.com.fiap.sereno;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest(properties = {

		"spring.rabbitmq.host=localhost",
		"spring.rabbitmq.port=1",
		"spring.rabbitmq.connection-timeout=2s",
		"spring.rabbitmq.listener.simple.auto-startup=false",
		"spring.ai.openai.api-key=teste-offline",
		"spring.ai.openai.base-url=http://localhost:9",
		"spring.ai.retry.max-attempts=1" })
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SerenoApiIntegracaoTest {

	private static final String SENHA = "Sereno@123";

	@Autowired
	private MockMvc mvc;

	private String login(String email) throws Exception {
		String corpo = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"senha\":\"" + SENHA + "\"}"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return "Bearer " + JsonPath.read(corpo, "$.token");
	}

	private String cidadao() throws Exception {
		return login("cidadao@sereno.app");
	}

	private String gestor() throws Exception {
		return login("gestor@sereno.app");
	}

	@Test
	@Order(1)
	@DisplayName("06 - Swagger/OpenAPI responde")
	void swaggerResponde() throws Exception {
		mvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("Sereno API"));
	}

	@Test
	@Order(2)
	@DisplayName("03 - Sem token: 401 no formato padronizado")
	void semTokenRetorna401() throws Exception {
		mvc.perform(get("/api/locais"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.path").value("/api/locais"));
	}

	@Test
	@Order(3)
	@DisplayName("03 - Senha errada: 401")
	void loginComSenhaErrada() throws Exception {
		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"cidadao@sereno.app\",\"senha\":\"errada123\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("E-mail ou senha inválidos"));
	}

	@Test
	@Order(4)
	@DisplayName("03 - Token do cidadão traz o papel CIDADAO")
	void meuPerfil() throws Exception {
		mvc.perform(get("/api/usuarios/me").header(HttpHeaders.AUTHORIZATION, cidadao()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("cidadao@sereno.app"))
				.andExpect(jsonPath("$.role").value("CIDADAO"));
	}

	@Test
	@Order(5)
	@DisplayName("01 - Listagem paginada {items, page, size, totalItems, totalPages}")
	void paginacao() throws Exception {
		mvc.perform(get("/api/locais").param("page", "0").param("size", "2").param("sort", "nome")
				.header(HttpHeaders.AUTHORIZATION, cidadao()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items", hasSize(2)))
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(2))
				.andExpect(jsonPath("$.totalItems").value(4))
				.andExpect(jsonPath("$.totalPages").value(2));
	}

	@Test
	@Order(6)
	@DisplayName("01/05 - Cidadão avalia e o indice_calma é recalculado pelo evento")
	void criarAvaliacaoRecalculaIndice() throws Exception {
		String token = cidadao();

		mvc.perform(get("/api/locais/3").header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.indiceCalma").value(nullValue()));

		mvc.perform(post("/api/avaliacoes").header(HttpHeaders.AUTHORIZATION, token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"localId": 3, "nivelRuido": 2, "nivelIluminacao": 2, "nivelLotacao": 1,
						 "periodoVisita": "MANHA", "comentario": "Jardins tranquilos de manhã."}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(notNullValue()));

		mvc.perform(get("/api/locais/3").header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.indiceCalma").value(8.25))
				.andExpect(jsonPath("$.classificacaoCalma").value("MUITO_CALMO"))
				.andExpect(jsonPath("$.totalAvaliacoes").value(1));
	}

	@Test
	@Order(7)
	@DisplayName("02 - Bean Validation: 400 com campos[]")
	void validacaoRetorna400() throws Exception {
		mvc.perform(post("/api/avaliacoes").header(HttpHeaders.AUTHORIZATION, cidadao())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"localId\": 3, \"nivelRuido\": 9, \"nivelIluminacao\": 2, \"nivelLotacao\": 1}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.campos[*].campo", hasItems("nivelRuido", "periodoVisita")))
				.andExpect(jsonPath("$.timestamp").value(notNullValue()));
	}

	@Test
	@Order(8)
	@DisplayName("02 - Avaliar o mesmo local duas vezes: 409")
	void avaliacaoDuplicadaRetorna409() throws Exception {
		mvc.perform(post("/api/avaliacoes").header(HttpHeaders.AUTHORIZATION, cidadao())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"localId": 1, "nivelRuido": 1, "nivelIluminacao": 1, "nivelLotacao": 1,
						 "periodoVisita": "TARDE"}
						"""))
				.andExpect(status().isConflict());
	}

	@Test
	@Order(9)
	@DisplayName("02 - Recurso inexistente: 404")
	void localInexistenteRetorna404() throws Exception {
		mvc.perform(get("/api/locais/999").header(HttpHeaders.AUTHORIZATION, cidadao()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensagem").value("Local 999 não encontrado"));
	}

	@Test
	@Order(10)
	@DisplayName("03 - Cidadão não edita local: 403")
	void cidadaoNaoEditaLocal() throws Exception {
		mvc.perform(put("/api/locais/1").header(HttpHeaders.AUTHORIZATION, cidadao())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nome": "Teste", "categoria": "OUTRO", "endereco": "Rua X", "latitude": 0, "longitude": 0}
						"""))
				.andExpect(status().isForbidden());
	}

	@Test
	@Order(10)
	@DisplayName("03 - Cidadão não exclui local: 403")
	void cidadaoNaoExcluiLocal() throws Exception {
		mvc.perform(delete("/api/locais/1").header(HttpHeaders.AUTHORIZATION, cidadao()))
				.andExpect(status().isForbidden());
	}

	@Test
	@Order(13)
	@DisplayName("01/03 - Cidadão cadastra local: 201")
	void cidadaoCriaLocal() throws Exception {
		mvc.perform(post("/api/locais").header(HttpHeaders.AUTHORIZATION, cidadao())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nome": "Restaurante Silencioso", "categoria": "RESTAURANTE",
						 "endereco": "Rua Augusta, 1000 - Consolação, São Paulo - SP",
						 "latitude": -23.5560, "longitude": -46.6620}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(notNullValue()))
				.andExpect(jsonPath("$.classificacaoCalma").value("SEM_DADOS"));
	}

	@Test
	@Order(11)
	@DisplayName("03 - Cidadão não edita avaliação de outra pessoa: 403")
	void cidadaoNaoEditaAvaliacaoDeOutro() throws Exception {

		mvc.perform(put("/api/avaliacoes/2").header(HttpHeaders.AUTHORIZATION, cidadao())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nivelRuido": 1, "nivelIluminacao": 1, "nivelLotacao": 1, "periodoVisita": "MANHA"}
						"""))
				.andExpect(status().isForbidden());
	}

	@Test
	@Order(12)
	@DisplayName("03 - Cidadão não lista usuários: 403 / Gestor lista: 200")
	void somenteGestorListaUsuarios() throws Exception {
		mvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, cidadao()))
				.andExpect(status().isForbidden());
		mvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, gestor()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalItems").value(notNullValue()));
	}

	@Test
	@Order(13)
	@DisplayName("01/03 - Gestor cadastra local: 201")
	void gestorCriaLocal() throws Exception {
		mvc.perform(post("/api/locais").header(HttpHeaders.AUTHORIZATION, gestor())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nome": "Museu da Língua Portuguesa", "categoria": "MUSEU",
						 "endereco": "Praça da Luz, s/n - Luz, São Paulo - SP",
						 "latitude": -23.5347, "longitude": -46.6338}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(notNullValue()))
				.andExpect(jsonPath("$.classificacaoCalma").value("SEM_DADOS"));
	}

	@Test
	@Order(14)
	@DisplayName("03/05 - Gestor modera (remove) avaliação e o índice é recalculado")
	void gestorRemoveAvaliacao() throws Exception {
		String token = gestor();

		mvc.perform(delete("/api/avaliacoes/5").header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isNoContent());
		mvc.perform(get("/api/locais/4").header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalAvaliacoes").value(0))
				.andExpect(jsonPath("$.indiceCalma").value(nullValue()));
	}

	@Test
	@Order(15)
	@DisplayName("03 - Gestor não cria avaliação: 403")
	void gestorNaoAvalia() throws Exception {
		mvc.perform(post("/api/avaliacoes").header(HttpHeaders.AUTHORIZATION, gestor())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"localId": 3, "nivelRuido": 1, "nivelIluminacao": 1, "nivelLotacao": 1,
						 "periodoVisita": "MANHA"}
						"""))
				.andExpect(status().isForbidden());
	}

	@Test
	@Order(16)
	@DisplayName("04 - Insight do local (offline cai no modo REGRAS)")
	void insightDoLocal() throws Exception {
		mvc.perform(get("/api/locais/2/insight").header(HttpHeaders.AUTHORIZATION, cidadao()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nomeLocal").value("Parque Ibirapuera"))
				.andExpect(jsonPath("$.indiceCalma").value(4.38))
				.andExpect(jsonPath("$.melhorPeriodo").value("MANHA"))
				.andExpect(jsonPath("$.indicePorPeriodo", hasSize(2)))
				.andExpect(jsonPath("$.fonte").value("REGRAS"));
	}

	@Test
	@Order(17)
	@DisplayName("01/03 - Cadastro público sempre cria CIDADAO")
	void cadastroCriaCidadao() throws Exception {
		mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"Maria Silva\",\"email\":\"maria@email.com\",\"senha\":\"Senha123\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.token").value(notNullValue()))
				.andExpect(jsonPath("$.usuario.role").value("CIDADAO"));
	}

}