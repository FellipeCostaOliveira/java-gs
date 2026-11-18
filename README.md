# Sereno API — CitizenTech

**Sereno** é um app de acessibilidade urbana: um **mapa sensorial** em que cidadãos avaliam lugares da cidade quanto a **ruído, iluminação e lotação**. Com essas avaliações a API calcula o **índice de calma (0 a 10)** de cada local e usa **IA generativa** para orientar pessoas com sensibilidade sensorial (autistas, pessoas com TDAH, enxaqueca, ansiedade, idosos, famílias com crianças pequenas) sobre **quando e como** visitar cada lugar com mais conforto.

Esta é a API backend (Java 21 + Spring Boot 3), a mesma consumida pelo app Mobile.

## Links da entrega
| Item | Link |
|---|---|
| Repositório | https://github.com/FellipeCostaOliveira/java-gs |
| API no ar (Render) | https://sereno-api-ayhx.onrender.com |
| Swagger UI | https://sereno-api-ayhx.onrender.com/swagger-ui.html |
| Vídeo pitch  | https://youtu.be/d1GEXt4JAF0 |

> No plano grátis do Render a API "dorme" após 15 min sem uso: a primeira chamada pode levar até 1 minuto.

## Integrantes
| Nome | RM |
|:---:|:---:|
| Eduardo Augusto de Oliveira Souza | RM565269 |
| Fellipe Costa de Oliveira | RM564673 |
| Felype Ferreira Maschio | RM563009 |
| Gustavo Vieira de Matos | RM563304 |
| Pedro Henrique dos Santos Costa | RM562156 |

## Opção escolhida no requisito 5
**Opção A: mensageria com RabbitMQ.** Cada avaliação criada, alterada ou removida publica o evento `AvaliacaoAlteradaEvent` na fila `sereno.avaliacoes`. Um consumidor assíncrono recebe o evento e recalcula o índice de calma do local. Detalhes em [Mensageria](#mensageria-avaliacaoalteradaevent).

---

## Sumário
1. [Tecnologias](#tecnologias)
2. [Como rodar](#como-rodar)
3. [Usuários de teste](#usuários-de-teste)
4. [Endpoints](#endpoints)
5. [Segurança (JWT + papéis)](#segurança-jwt--papéis)
6. [Spring AI: insight sensorial](#spring-ai-insight-sensorial)
7. [Mensageria: AvaliacaoAlteradaEvent](#mensageria-avaliacaoalteradaevent)
8. [Paginação e erros](#paginação-e-erros)
9. [Integração com o app Mobile](#integração-com-o-app-mobile)
10. [Mapa dos requisitos](#mapa-dos-requisitos)
11. [Declaração de uso de IA](#declaração-de-uso-de-ia)
---

## Tecnologias
| Tecnologia | Uso |
|---|---|
| Java 21, Spring Boot 3.5 | Base da API |
| Spring Data JPA | Entidades `Usuario`, `Local`, `Avaliacao` (tabelas `SER_USUARIO`, `SER_LOCAL`, `SER_AVALIACAO`), `ddl-auto=validate` |
| H2 (padrão) / Oracle (perfil `oracle`) | Banco de dados |
| Flyway | Cria as tabelas e os dados de exemplo |
| Bean Validation + `@RestControllerAdvice` | Validação de DTOs e erros padronizados |
| Spring Security + JWT HS256 | Autenticação stateless, papéis `CIDADAO` e `GESTOR` |
| Spring AI (OpenAI `gpt-4o-mini`) | Insight sensorial de cada local |
| RabbitMQ | Evento `AvaliacaoAlteradaEvent` → recálculo assíncrono do `indice_calma` |
| springdoc-openapi | Swagger UI |

Pacotes (mesma organização usada em aula): `control`, `model`, `repository`, `service`, `security`, `mensageria`, `record`, `exception`, `config`.

```
 App Mobile ──HTTP + Bearer JWT──► control ─► service ─► repository ─► Banco (SER_*)
                                                │
                                                ├─► InsightService ─► Spring AI ─► LLM
                                                │
                     AvaliacaoService ─ evento (após commit) ─► RabbitMQProdutor
                                                                     │
                            exchange sereno.exchange ─► fila sereno.avaliacoes
                                                                     │
                       RabbitMQConsumidor ─► IndiceCalmaService.recalcular(localId)
```

## Como rodar

### Pré-requisitos
- **JDK 21**
- (Opcional) **Docker** para o RabbitMQ
- (Opcional) chave da OpenAI para o insight com IA

Não é preciso instalar o Maven: o projeto traz o Maven Wrapper (`mvnw`).

### 1. Rodar (sem configurar nada)
```bash
./mvnw spring-boot:run          # Linux/macOS
mvnw.cmd spring-boot:run        # Windows
```
No Eclipse: botão direito em `SerenoApplication` → *Run As → Spring Boot App* (ou *Java Application*). No IntelliJ: rodar a classe `SerenoApplication`.

A API sobe com **H2 em memória** (igual ao projeto da aula). O Flyway cria as tabelas e os dados de exemplo a cada execução.

- Swagger UI: **http://localhost:8080/swagger-ui.html**
- Health: http://localhost:8080/actuator/health
- Requisições prontas: [`sereno.http`](sereno.http) (IntelliJ / VS Code REST Client)

### 2. Ligar o RabbitMQ (mensageria)
```bash
docker compose up -d rabbitmq
```
Painel: http://localhost:15672 (guest / guest). Com o RabbitMQ no ar, o recálculo do índice acontece **de forma assíncrona** pela fila. Sem ele, a API continua funcionando e recalcula o índice na hora (fallback), registrando um aviso no log.

### 3. Ligar a IA
Abra `src/main/resources/application.properties` e troque `COLE_SUA_CHAVE_AQUI` pela chave da OpenAI:
```properties
spring.ai.openai.api-key=sk-proj-...
```
Reinicie a API. Enquanto a chave não for colocada, a API sobe normalmente e o `/insight` responde com `"fonte": "REGRAS"` (resumo montado por regras).

### Variáveis opcionais (`.env` ou variáveis de ambiente)
| Variável | Para que serve |
|---|---|
| `JWT_SECRET` | Segredo HS256 (32+ caracteres). Sem ele, a API gera um segredo aleatório a cada execução e os tokens deixam de valer quando ela reinicia |
| `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` | Padrão `localhost:5672 guest/guest` |

### Usar o Oracle em vez do H2 (opcional)
Preencha no `.env`:
```
DB_USERNAME=RM000000
DB_PASSWORD=sua_senha_do_oracle
```
E rode com o perfil `oracle`:
```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=oracle
```
(Eclipse: *Run Configurations → Arguments → Program arguments* `--spring.profiles.active=oracle`; IntelliJ: *Active profiles* `oracle`.) Os scripts Oracle são idempotentes e o histórico do Flyway fica em `SER_FLYWAY_HISTORICO`. Se aparecer `ORA-01017`, o usuário ou a senha do Oracle estão errados. Use valores sem aspas e sem espaço no fim da linha.

### Testes
```bash
mvnw.cmd test
```
No Eclipse: botão direito em `src/test/java` → *Run As → JUnit Test*. O `SerenoApiIntegracaoTest` sobe a API inteira (H2) e testa, de ponta a ponta, cada requisito: Swagger, 401/403 por papel, paginação, Bean Validation (400/404/409), criação de avaliação com evento e recálculo do índice, moderação do gestor e insight.

## Deploy no Render (para o app Mobile)
A API está publicada no Render (link em [Links da entrega](#links-da-entrega)). O projeto tem um `Dockerfile` pronto. No Render: *New → Web Service* → conecte o repositório do GitHub → **Language: Docker** → **Root Directory: `sereno`** → **Instance Type: Free**. Em *Environment Variables*, configure:

| Variável | Valor |
|---|---|
| `SPRING_AI_OPENAI_APIKEY` | sua chave da OpenAI (sobrescreve o valor do `application.properties`) |
| `JWT_SECRET` | um texto aleatório com 32+ caracteres, o mesmo usado pela API .NET |
| `MANAGEMENT_HEALTH_RABBIT_ENABLED` | `false`, quando não houver RabbitMQ na nuvem (evita que o health check fique DOWN) |
| `RABBITMQ_HOST`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD`, `SPRING_RABBITMQ_VIRTUALHOST` | opcional: dados do RabbitMQ na nuvem (ex.: CloudAMQP, plano grátis) |

*Health Check Path:* `/actuator/health`. Cada `push` na branch `main` dispara um novo deploy automaticamente.
O banco H2 volta aos dados iniciais quando o serviço reinicia.

## Usuários de teste
Senha de todos: **`Sereno@123`**

| E-mail | Papel |
|---|---|
| `gestor@sereno.app` | GESTOR |
| `cidadao@sereno.app` | CIDADAO |
| `ana@sereno.app` | CIDADAO |

No Swagger: faça `POST /api/auth/login`, copie o `token`, clique em **Authorize** e cole.

## Endpoints

| Método | Rota | Papel | Descrição |
|---|---|---|---|
| POST | `/api/auth/register` | público | Cadastra **CIDADAO** e retorna token (201) |
| POST | `/api/auth/login` | público | Retorna JWT |
| GET | `/api/usuarios/me` | CIDADAO, GESTOR | Perfil do usuário logado |
| PUT | `/api/usuarios/me` | CIDADAO, GESTOR | Atualiza o nome |
| GET | `/api/usuarios/me/avaliacoes` | CIDADAO, GESTOR | Minhas avaliações (paginado) |
| GET | `/api/usuarios` | **GESTOR** | Lista usuários (paginado) |
| PATCH | `/api/usuarios/{id}/role` | **GESTOR** | Promove/rebaixa usuário |
| GET | `/api/locais?nome=&categoria=&indiceMinimo=&page=&size=&sort=` | CIDADAO, GESTOR | Lista locais (paginado + filtros) |
| GET | `/api/locais/{id}` | CIDADAO, GESTOR | Detalhe |
| POST | `/api/locais` | CIDADAO, GESTOR | Cria (201 + Location). O app só envia lugares escolhidos no mapa (OpenStreetMap) |
| PUT | `/api/locais/{id}` | **GESTOR** | Atualiza |
| DELETE | `/api/locais/{id}` | **GESTOR** | Remove o local e suas avaliações (204) |
| GET | `/api/locais/{id}/insight` | CIDADAO, GESTOR | **Insight com Spring AI** |
| GET | `/api/avaliacoes?localId=&page=&size=&sort=` | CIDADAO, GESTOR | Lista avaliações (paginado) |
| GET | `/api/avaliacoes/{id}` | CIDADAO, GESTOR | Detalhe |
| POST | `/api/avaliacoes` | **CIDADAO** | Cria (1 por local por usuário) → evento |
| PUT | `/api/avaliacoes/{id}` | **CIDADAO (dono)** | Atualiza → evento |
| DELETE | `/api/avaliacoes/{id}` | dono ou **GESTOR** (moderação) | Remove → evento |

**Categorias:** `PARQUE, BIBLIOTECA, MUSEU, CENTRO_CULTURAL, SHOPPING, RESTAURANTE, CAFE, TRANSPORTE, SAUDE, SERVICO_PUBLICO, EDUCACAO, OUTRO`
**Períodos:** `MANHA, TARDE, NOITE`

### Índice de calma
```
estimulo = 0.5 × ruído + 0.2 × iluminação + 0.3 × lotação     (escala 1 a 5)
indice   = (5 − estimulo) × 2.5                                 (0 a 10)
```
O ruído tem o maior peso por ser o principal gatilho de sobrecarga sensorial. Classificação para o mapa: `MUITO_CALMO` (≥ 8), `CALMO` (≥ 6), `MODERADO` (≥ 4), `AGITADO` (< 4), `SEM_DADOS`.

## Segurança (JWT + papéis)
- JWT **HS256**, `iss = sereno-auth`, `aud = sereno-app`, claims `sub` (id do usuário), `email`, `role` (`CIDADAO` | `GESTOR`), `jti`, `iat`, `nbf`, `exp`.
- Validação de assinatura, expiração, emissor, audiência e papel em toda requisição.
- Nenhum segredo fixo no código: `JWT_SECRET` vem do ambiente (ou é gerado aleatoriamente por execução).
- Senhas com **BCrypt**; o cadastro público **nunca** cria GESTOR (só um gestor promove outro usuário).
- Regras por papel no `SecurityFilterChain` **e** com `@PreAuthorize` nos controllers; regra de dono (editar só a própria avaliação) no service.
- API stateless, CSRF desabilitado (sem cookies), CORS configurável (`CORS_ORIGINS`), Actuator expõe só `health` e `info`.
- 401 e 403 retornam o mesmo JSON padronizado de erro.

## Spring AI: insight sensorial
`GET /api/locais/{id}/insight` não é um chat genérico: faz parte do fluxo do app. A API junta:
- índice de calma e médias gerais de ruído, iluminação e lotação;
- índice **por período do dia** (manhã/tarde/noite);
- até 20 comentários recentes dos cidadãos (sanitizados e marcados como dados, para mitigar *prompt injection*);

e pede ao LLM uma resposta **estruturada** (`BeanOutputConverter`) com `resumo`, `melhorPeriodo`, `pontosDeAtencao`, `dicas` e `perfilRecomendado`. O resultado fica em cache até o próximo recálculo do índice (disparado pelo consumidor RabbitMQ). Se a IA estiver indisponível, a resposta é montada por regras (`fonte: "REGRAS"`) para o app nunca ficar sem informação.

Exemplo de resposta:
```json
{
  "localId": 2,
  "nomeLocal": "Parque Ibirapuera",
  "indiceCalma": 4.38,
  "classificacaoCalma": "MODERADO",
  "totalAvaliacoes": 2,
  "indicePorPeriodo": [
    { "periodo": "MANHA", "totalAvaliacoes": 1, "indiceCalma": 7.00 },
    { "periodo": "TARDE", "totalAvaliacoes": 1, "indiceCalma": 1.75 }
  ],
  "resumo": "De manhã o parque é tranquilo e muito verde; à tarde, principalmente nos fins de semana, fica lotado e com música alta.",
  "melhorPeriodo": "MANHA",
  "pontosDeAtencao": ["Lotação à tarde", "Som alto perto da marquise"],
  "dicas": ["Prefira ir cedo", "Evite a região da marquise à tarde", "Leve protetor auricular"],
  "perfilRecomendado": "Indicado pela manhã para quem busca ambientes calmos ao ar livre.",
  "fonte": "IA",
  "geradoEm": "2026-11-09T10:00:00-03:00"
}
```

## Mensageria: AvaliacaoAlteradaEvent
1. `POST/PUT/DELETE /api/avaliacoes` grava a avaliação e publica `AvaliacaoAlteradaEvent { avaliacaoId, localId, usuarioId, tipo (CRIADA|ATUALIZADA|REMOVIDA), ocorridoEm }`.
2. `RabbitMQProdutor` envia o evento **somente após o commit** (`@TransactionalEventListener(AFTER_COMMIT)`) para a exchange `sereno.exchange` → fila **`sereno.avaliacoes`** (JSON).
3. `RabbitMQConsumidor` (`@RabbitListener`) consome de forma assíncrona e chama `IndiceCalmaService.recalcular(localId)`, que recalcula `INDICE_CALMA` e `TOTAL_AVALIACOES` com base em todas as avaliações do local.
4. Falhas são tentadas 3 vezes e depois vão para a DLQ `sereno.avaliacoes.dlq`.
5. Se o broker estiver fora do ar no momento da publicação, o índice é recalculado de forma síncrona (o mapa não fica desatualizado).

Para ver acontecendo: crie uma avaliação e acompanhe o log (`Evento publicado...` / `Evento recebido...` / `indice_calma do local X recalculado`) ou o painel do RabbitMQ.

## Paginação e erros
Todas as listagens aceitam `page` (base 0), `size` (máx. 100) e `sort` (ex.: `sort=nome,asc`) e respondem:
```json
{ "items": [ ... ], "page": 0, "size": 10, "totalItems": 42, "totalPages": 5 }
```
Todos os erros (validação, 401, 403, 404, 409, 422, 500…) respondem:
```json
{
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Um ou mais campos são inválidos",
  "campos": [ { "campo": "latitude", "mensagem": "A latitude deve estar entre -90 e 90" } ],
  "timestamp": "2026-11-09T10:00:00-03:00",
  "path": "/api/locais"
}
```

## Integração com o app Mobile
- **Base URL (produção):** `https://COLE-AQUI-O-LINK-DO-RENDER.onrender.com/api`.
- **Base URL (local):** `http://<IP-da-máquina>:8080/api` (no emulador Android use `http://10.0.2.2:8080/api`; no celular físico use o IP da máquina na mesma rede).
- Fluxo: `POST /api/auth/login` → guarde `token` (ex.: SecureStore) → envie `Authorization: Bearer <token>` em todas as chamadas.
- Telas sugeridas → endpoints: Mapa (`GET /api/locais?size=100`, cor pelo `classificacaoCalma`), Detalhe do local (`GET /api/locais/{id}` + `GET /api/locais/{id}/insight`), Avaliar (`POST /api/avaliacoes`), Minhas avaliações (`GET /api/usuarios/me/avaliacoes`), Perfil (`GET /api/usuarios/me`).
- Exiba `mensagem` e `campos[]` do erro padronizado direto nos formulários; em `401`, volte para o login.

```ts
// api.ts (React Native / Expo)
import axios from "axios";
import * as SecureStore from "expo-secure-store";

export const api = axios.create({ baseURL: "http://10.0.2.2:8080/api" });

api.interceptors.request.use(async (config) => {
  const token = await SecureStore.getItemAsync("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export async function login(email: string, senha: string) {
  const { data } = await api.post("/auth/login", { email, senha });
  await SecureStore.setItemAsync("token", data.token);
  return data.usuario;
}
```

## Mapa dos requisitos
| # | Requisito | Onde está |
|---|---|---|
| 01 | 3+ entidades relacionadas, CRUD com Spring Data JPA, paginação | `Usuario`, `Local`, `Avaliacao` (`model`), repositórios, `PageResponse` |
| 02 | Bean Validation nos DTOs + `@RestControllerAdvice` padronizado | `record/*Request`, `exception/GlobalExceptionHandler`, `security/ErroSegurancaHandler` |
| 03 | Spring Security + JWT, 2 papéis, endpoints por papel | `security/SegurancaConfig`, `JwtConfig`, `TokenService`, `@PreAuthorize` |
| 04 | Spring AI integrado ao fluxo | `service/InsightService`, `GET /api/locais/{id}/insight` |
| 05 | Arquitetura distribuída (A — RabbitMQ) | `mensageria/*`, fila `sereno.avaliacoes`, recálculo do `indice_calma` |
| 06 | Swagger, README, consumo pelo Mobile | springdoc (`/swagger-ui.html`), este README, seção Mobile |

## Declaração de uso de IA
Usamos IA de duas formas neste projeto:

1. **Como funcionalidade da API:** o endpoint `GET /api/locais/{id}/insight` usa Spring AI com o modelo `gpt-4o-mini` da OpenAI para gerar o insight sensorial de cada local (requisito 04).
2. **Como ferramenta de apoio ao desenvolvimento:** usamos o assistente Claude (Anthropic) para gerar e revisar partes do código e dos testes automatizados. Todo o conteúdo foi revisado, executado e testado pelo grupo, que validou os testes JUnit, o Swagger, a integração com a OpenAI, o RabbitMQ e o deploy no Render. O grupo é responsável pelas decisões e pelo resultado final.
