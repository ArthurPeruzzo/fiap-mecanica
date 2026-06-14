# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Stack

- Java 25 + Spring Boot 4.0.5
- Spring Web MVC, Spring Security, Spring Data JPA
- MySQL via Flyway migrations
- Lombok, Auth0 `java-jwt`, SpringDoc OpenAPI
- Maven wrapper (`./mvnw`)

## Commands

```bash
# Build
./mvnw package

# Run (requires DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET env vars or defaults apply)
./mvnw spring-boot:run

# Run all tests — must use Java 25 explicitly (system default may be older)
JAVA_HOME=/home/arthur/.jdks/jdk-25 ./mvnw test

# Run a single test class or method
JAVA_HOME=/home/arthur/.jdks/jdk-25 ./mvnw test -Dtest=ClassName
JAVA_HOME=/home/arthur/.jdks/jdk-25 ./mvnw test -Dtest=ClassName#methodName
```

## Docker

```bash
# Build image and start app + MySQL
docker compose up --build

# Start without rebuilding
docker compose up

# Stop
docker compose down

# Stop and delete MySQL data volume
docker compose down -v
```

MySQL is exposed on the **host at port 3307** (mapped from container port 3306). Connect with DBeaver at `127.0.0.1:3307`, user `root`, password `root`, database `mecanica`. The app connects to MySQL via the service name `db` inside the compose network.

## Default users

Seeded by Flyway migrations. All share the same password `MeCanica2026!@#`.

| E-mail | Role |
|---|---|
| `administrador@mecanica.com` | `ROLE_ADMINISTRADOR` |
| `atendente@mecanica.com` | `ROLE_ATENDENTE` |
| `mecanico@mecanica.com` | `ROLE_MECANICO` |

Login via `POST /authenticate/login`. Migration `V14` seeds sample data (clients, vehicles, parts, supplies, services, orders in every status) for immediate API exploration. Swagger UI: `http://localhost:8080/swagger-ui.html`.

## Environment variables

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:mysql://127.0.0.1:3306/mecanica` | JDBC URL |
| `DB_USERNAME` | `root` | DB user |
| `DB_PASSWORD` | `root` | DB password |
| `JWT_SECRET` | — | Base64URL-encoded secret (min 32 bytes decoded). Required in production. |

Flyway migrations: `src/main/resources/db/migration/V{version}__{description}.sql`

## Architecture

The project follows **Clean Architecture** with a strict split between `core` (domain) and `infra` (framework/DB/HTTP). There are four top-level business modules alongside `shared`:

```
com.fiap.mecanica
├── shared/
│   ├── exception/          # BaseException, GlobalExceptionHandler, ErroAcessoBaseDeDadosException
│   ├── valueobjects/       # NomeCompleto, Documento (interface), Cpf, Cnpj
│   ├── page/               # Pagina<T> (plain Java, used in core) + PageResponse (infra, Spring-based)
│   ├── swagger/            # OpenApiConfig
│   ├── notificacao/        # NotificacaoGateway (core/gateway/) + NotificacaoMockGateway (infra/)
│   └── seguranca/          # Security/auth module (see below)
├── gestao/                 # Business module: clients, vehicles, mechanics, attendants
│   ├── core/
│   │   ├── domain/         # Cliente, Veiculo + Placa (value object), Mecanico, Atendente
│   │   ├── dto/            # Criar/Atualizar/ListarClienteDto, Criar/Atualizar/ListarVeiculoDto
│   │   ├── gateway/        # ClienteGateway, VeiculoGateway, MecanicoGateway, AtendenteGateway
│   │   ├── usecase/        # Criar/Atualizar/Deletar/Listar for Cliente and Veiculo
│   │   └── exception/      # ClienteJaExiste/NaoEncontrado, VeiculoJaExiste/NaoEncontrado
│   └── infra/
│       ├── controller/     # ClienteController, VeiculoController
│       │                   # validation/: @DocumentoValido (class-level), @PlacaValida (field-level)
│       └── gateway/
│           ├── database/   # ClienteDatabaseGateway, VeiculoDatabaseGateway, etc.
│           ├── entity/     # ClienteEntity (@OneToMany veiculos), VeiculoEntity (@ManyToOne cliente), etc.
│           └── repository/ # ClienteRepository, VeiculoRepository, MecanicoRepository, AtendenteRepository
├── estoque/                # Business module: parts (peças) and supplies (insumos)
│   ├── core/
│   │   ├── domain/         # ItemEstoque (abstract), Peca extends ItemEstoque, Insumo extends ItemEstoque, UnidadeMedida (enum)
│   │   ├── dto/            # Criar/Atualizar/Listar for Peca and Insumo
│   │   ├── gateway/        # PecaGateway, InsumoGateway
│   │   ├── usecase/        # Criar/Atualizar/Deletar/Listar for Peca and Insumo
│   │   └── exception/      # PecaNaoEncontrada, InsumoNaoEncontrado, EstoqueInsuficiente
│   └── infra/              # PecaController, InsumoController, entities, repositories
└── ordemdeservico/         # Business module: service orders and services
    ├── core/
    │   ├── domain/
    │   │   ├── ordemdeservico/ # OrdemDeServico + state machine, ServicoVinculado, PecaVinculada,
    │   │   │                   # InsumoVinculado records, Orcamento record
    │   │   └── servico/        # Servico
    │   ├── dto/            # CriarOrdemDeServicoDto, OrdemDeServicoListagemDto,
    │   │               # ServicoVinculadoDto, PecaVinculadaDto, InsumoVinculadoDto,
    │   │               # Criar/Atualizar/ListarServicoDto
    │   ├── gateway/        # OrdemDeServicoGateway, ServicoGateway
    │   ├── usecase/
    │   │   ├── ordemdeservico/ # CriarOrdemDeServico, IniciarDiagnostico, ConcluirDiagnostico,
    │   │   │                   # VincularServico, DesvincularServico,
    │   │   │                   # VincularPeca, DesvincularPeca, VincularInsumo, DesvincularInsumo,
    │   │   │                   # EnviarOrcamentoOrdemDeServico,
    │   │   │                   # OrcamentoAprovadoOrdemDeServico, OrcamentoRecusadoOrdemDeServico,
    │   │   │                   # IniciarServicoOrdemDeServico, FinalizarServicoOrdemDeServico,
    │   │   │                   # EntregarOrdemDeServico, ListarOrdemDeServico
    │   │   └── servico/        # Criar/Atualizar/Deletar/ListarServico
    │   └── exception/      # OrdemDeServicoNaoEncontrada, TransicaoDeStatusInvalida,
    │                       # VinculoServico/Peca/InsumoNaoAutorizado,
    │                       # DesvincularPeca/InsumoNaoAutorizada,
    │                       # PecaNaoVinculada, InsumoNaoVinculado,
    │                       # QuantidadeDesvincularInvalida,
    │                       # IniciarServicoNaoAutorizado, FinalizarServicoNaoAutorizado,
    │                       # ServicoEmExecucaoOuFinalizado, ServicoNaoIniciadoOuFinalizado,
    │                       # ServicoNaoVinculado, OrdemDeServicoSemServicos,
    │                       # MecanicoNaoResponsavelPelaOrdemDeServico,
    │                       # VeiculoNaoPertenceAoCliente, etc.
    └── infra/
        ├── controller/     # OrdemDeServicoController, ServicoController
        │                   # json/: request JSONs including VincularPecaRequestJson,
        │                   #        DesvincularPecaRequestJson, VincularInsumoRequestJson,
        │                   #        DesvincularInsumoRequestJson (all carry Integer quantidade)
        └── gateway/
            ├── database/   # OrdemDeServicoDatabaseGateway
            ├── entity/     # OrdemDeServicoEntity, ServicoEntity,
            │               # OrdemDeServicoServicoEntity (join: ordem_servico_servico — stores preco, StatusServico, timestamps),
            │               # OrdemDeServicoPecaEntity, OrdemDeServicoInsumoEntity
            └── repository/ # OrdemDeServicoRepository, ServicoRepository,
                            # OrdemDeServicoServicoRepository,
                            # OrdemDeServicoPecaRepository, OrdemDeServicoInsumoRepository
```

`shared/seguranca/` follows the same core/infra split:
- `core/`: `User`, `Email`, `Role`, `RoleEnum`, `Password`/`PasswordHash` value objects, `UserGateway`, `TokenGateway`, `AuthenticateUserUseCase`, domain exceptions
- `infra/`: `SecurityConfiguration`, `UserAuthenticationFilter`, `JwtTokenService`, `AuthenticateController`, JPA entities/repositories

`shared/notificacao/` has a single gateway interface `NotificacaoGateway.enviarOrcamento(Long clienteId, BigDecimal valorTotal)`. The only implementation is `NotificacaoMockGateway`, which logs via SLF4J.

### Key architectural rules

- **`core/` use cases têm `@Service` como concessão consciente.** Todos os use cases em `*/core/usecase/` usam `@Service` para integração com o container Spring. Isso viola a regra estrita de que `core/` não pode depender do framework, mas foi mantido por simplicidade. Não adicionar outras anotações Spring em `core/` — essa concessão é limitada a `@Service` nos use cases.
- **`core/` has zero Spring/JPA dependencies** (exceto `@Service` nos use cases — ver concessão acima). Domain objects are plain Java. JPA entities live exclusively in `infra/gateway/entity/`.
- **Dependency inversion via gateway interfaces.** All interfaces are defined in `core/gateway/`; implementations live in `infra/gateway/database/`.
- **Use cases** depend only on domain objects and gateway interfaces — never on Spring beans or JPA directly.
- **Cross-module gateway access is allowed.** `ordemdeservico` use cases depend on `gestao` gateways (e.g. `CriarOrdemDeServicoUseCase` uses `AtendenteGateway`, `ClienteGateway`, `VeiculoGateway`). This is intentional — `core/` modules may import from other modules' `core/` packages. For listing/read use cases that need human-readable fields from related modules (names, plate, etc.), enrich via cross-module gateways inside the use case and return a dedicated DTO (e.g. `ListarOrdemDeServicoUseCase` → `OrdemDeServicoListagemDto`). Never expose raw IDs in response JSON when the display name is available.
- **Value objects strip formatting on construction.** `Cpf` and `Cnpj` strip `[.\\-/]`; `Placa` strips `-` and uppercases — do not change to `\\D`.
- **`Placa` value object** lives in `gestao/core/domain/`. `getValor()` returns the stored (stripped) value; `getValorFormatado()` inserts `-` after the 3rd character for display.
- **Pagination boundary:** `Pagina<T>` (plain Java record in `shared/page/`) is used in `core/` and returned by gateways. `PageResponse` (Spring-based) lives in `infra/` and is used only in controllers. Controller converts via `PageResponse.from(pagina.map(ResponseJson::from))`.
- **Domain rehydration:** use the static factory `reconstituir(Long id, ...)` to rebuild domain objects from DB. For simple domain objects (`Cliente`, `Veiculo`, etc.) it calls the regular constructor then sets `id` directly. `OrdemDeServico.reconstituir` uses a private no-arg constructor and assigns all fields directly (bypassing the creation constructor intentionally — the `//TODO resolver esse metodo` comment marks this as a known debt). Never invent new bypass patterns.
- **Domain mutation:** use the `atualizar(...)` method on the domain object to update its state. The method re-validates invariants (e.g. exactly one document, valid plate format).
- **`Veiculo.clienteId` is immutable after creation.** `atualizar(placa, modelo, ano)` does not accept `clienteId` — the client link can only be changed by deleting and recreating the vehicle.
- **Domain exceptions** extend `BaseException` (which carries `HttpStatus`). Place them in `<module>/core/exception/`. `GlobalExceptionHandler` maps `BaseException` → its status, `MethodArgumentNotValidException` → 400, `DataIntegrityViolationException` → 400.
- **`GlobalExceptionHandler` global error priority:** class-level Bean Validation errors (e.g. `@DocumentoValido`) are returned first; field errors are only returned if there are no global errors.
- **Database gateway error handling:** wrap all repository calls in try/catch and rethrow as `ErroAcessoBaseDeDadosException` for infrastructure failures.
- **Duplicate check on update:** use `existsBy<Field>AndIdNot(value, id)` Spring Data derived queries to exclude the current entity from the uniqueness check.
- **JPA cascade delete:** `ClienteEntity` owns `@OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true) List<VeiculoEntity> veiculos`. When saving a vehicle, pass a minimal reference: `ClienteEntity.builder().id(clienteId).build()`.
- **Roles** are seeded by Flyway (`V1__create_security_struct.sql`): `ROLE_ATENDENTE`, `ROLE_MECANICO`, `ROLE_ADMINISTRADOR`. The `SecurityConfiguration` role constants must match `RoleEnum`. `ROLE_ADMINISTRADOR` controls `/cliente/**`, `/veiculo/**`, `/peca/**`, `/insumo/**`, `/servico/**`, and `GET /ordem-servico/detalhamento`. `ROLE_ATENDENTE` controls `POST /ordem-servico`, `POST /ordem-servico/orcamento/**`, and `PATCH /ordem-servico/*/entregar`. `ROLE_MECANICO` controls all `PATCH /ordem-servico/*/diagnostico*`, vincular/desvincular serviço/peça/insumo, and iniciar/finalizar serviço endpoints. Any unmatched request is denied (`anyRequest().denyAll()`).

### OrdemDeServico state machine

`OrdemDeServico` uses the **State pattern**. The domain object holds a `state: OrdemDeServicoState` (interface) that governs which transitions are allowed. Each state implements only the transitions it permits; the default throws `TransicaoDeStatusInvalidaException`.

```
RECEBIDA → (iniciarDiagnostico) → EM_DIAGNOSTICO → (concluirDiagnostico) → DIAGNOSTICO_CONCLUIDO → (enviarOrcamento) → AGUARDANDO_APROVACAO
                                                                                                                               ├─ (cancelar) → CANCELADA
                                                                                                                               └─ (aprovar)  → EM_EXECUCAO → (finalizar*) → FINALIZADA → (entregar) → ENTREGUE
```
\* `finalizar` is called automatically by `finalizarServico` when all linked `ServicoVinculado` entries reach `StatusServico.FINALIZADO`.

- `OrdemDeServicoStateFactory.from(status)` reconstructs the correct state object from a persisted `StatusOrdemDeServico` enum value — called inside `reconstituir(...)`.
- Transitions mutate the domain object via package-private setters (`setMecanicoId`, `setDataInicioDiagnostico`, etc.) that only state classes can call. New setters added: `setDataCancelamento`, `setDataAprovacao`, `setDataFinalizacao`, `setDataEntrega`.
- Business rules enforced before/during transitions: only the same mecânico who started can continue (`isMecanicoResponsavel`); a new OS cannot be opened for a vehicle that already has an open one; a mecânico cannot start diagnosis if another is already assigned.
- `concluirDiagnostico` is split: the `OrdemDeServico` domain method checks `isMecanicoResponsavel` and `servicosVinculados.isEmpty()` (throwing `MecanicoNaoResponsavelPelaOrdemDeServicoException` and `OrdemDeServicoSemServicosException` respectively), then delegates to `OrdemDeServicoEmDiagnosticoState.concluirDiagnostico()`, which calls `calcularOrcamento()` and transitions to `DIAGNOSTICO_CONCLUIDO`. `calcularOrcamento()` sums prices across `ServicoVinculado`, `PecaVinculada` (`preco * quantidade`), and `InsumoVinculado` (`preco * quantidade`) and sets `Orcamento orcamento` (record with a single `BigDecimal valorTotal`).
- `enviarOrcamento` (in `OrdemDeServicoDiagnosticoConcluidoState`) sets `dataEnvioOrcamento` and transitions to `AGUARDANDO_APROVACAO`.
- `cancelar` (in `OrdemDeServicoAguardandoAprovacaoState`) sets `dataCancelamento` and transitions to `CANCELADA`.
- `aprovar` (in `OrdemDeServicoAguardandoAprovacaoState`) sets `dataAprovacao` and transitions to `EM_EXECUCAO`.
- `finalizar` (in `OrdemDeServicoEmExecucaoState`) sets `dataFinalizacao` and transitions to `FINALIZADA`.
- `entregar` (in `OrdemDeServicoFinalizadaState`) sets `dataEntrega` and transitions to `ENTREGUE`.

**`OrdemDeServico` computed helpers:**
- `getValorTotalOrcamento()` — returns `orcamento.valorTotal()` or null.
- `calcularTempoMedioExecucaoServicos()` — averages `Duration` across all `ServicoVinculado` entries that have both `dataInicioExecucao` and `dataFimExecucao` set; returns null if none are finalized. Consumed by `ListarOrdemDeServicoUseCase` and surfaced as `TempoMedioExecucaoResponseJson` (decomposed into `dias`, `horas`, `minutos`).

**`ServicoVinculado` record** carries `(Long servicoId, BigDecimal preco, StatusServico status, LocalDateTime dataInicioExecucao, LocalDateTime dataFimExecucao)`. `StatusServico` values: `NAO_INICIADO`, `EM_EXECUCAO`, `FINALIZADO`.

**Service execution within `EM_EXECUCAO`:**
- `iniciarServico(Long servicoId)`: only allowed when `status == EM_EXECUCAO`; requires the service to be `NAO_INICIADO`; replaces the `ServicoVinculado` entry with `EM_EXECUCAO` + `dataInicioExecucao = now()`.
- `finalizarServico(Long servicoId)`: only allowed when `status == EM_EXECUCAO`; requires the service to be `EM_EXECUCAO`; replaces the entry with `FINALIZADO` + `dataFimExecucao = now()`. Then checks `todosOsServicosFinalizados()` — if true, calls `state.finalizar(this)` to auto-transition to `FINALIZADA`.

### OrdemDeServico — peças and insumos

`OrdemDeServico` holds `List<PecaVinculada>` and `List<InsumoVinculado>` (both plain Java records with `id`, `quantidade`, and `preco`). `vincularServico`, `vincularPeca`, and `vincularInsumo` are permitted from both `RECEBIDA` and `EM_DIAGNOSTICO`. `desvincularPeca` and `desvincularInsumo` are only permitted from `EM_DIAGNOSTICO`. `desvincularServico` is permitted from both `RECEBIDA` and `EM_DIAGNOSTICO`.

**`reconstituir` signature** (19 parameters — keep in sync when calling in tests):
```java
OrdemDeServico.reconstituir(id, clienteId, veiculoId, atendenteId, mecanicoId, status,
    descricao, dataCriacao, dataInicioDiagnostico, dataConclusaoDiagnostico,
    List<ServicoVinculado> servicosVinculados, List<PecaVinculada> pecasVinculadas,
    List<InsumoVinculado> insumosVinculados, Orcamento orcamento,
    LocalDateTime dataEnvioOrcamento, LocalDateTime dataCancelamento,
    LocalDateTime dataAprovacao, LocalDateTime dataFinalizacao,
    LocalDateTime dataEntrega)
```

**Domain rules for vincular/desvincular peça and insumo:**
- `vincularServico(Long servicoId, BigDecimal preco)`: allowed from `RECEBIDA` or `EM_DIAGNOSTICO`; adds a `ServicoVinculado` with `status = NAO_INICIADO` and null execution timestamps; throws `ServicoJaVinculadoException` if already linked.
- `desvincularServico(Long servicoId)`: allowed from `RECEBIDA` or `EM_DIAGNOSTICO`.
- `vincularPeca(Long pecaId, Integer quantidade, BigDecimal preco)` / `vincularInsumo(...)`: allowed from `RECEBIDA` or `EM_DIAGNOSTICO`; if the item is already linked, sums the quantity instead of adding a duplicate entry.
- `desvincularPeca(Long pecaId, Integer quantidade)`: throws `PecaNaoVinculadaException` if not linked; throws `QuantidadeDesvincularInvalidaException` if `quantidade > existente.quantidade()`; removes the entry entirely when `quantidade == existente.quantidade()`; preserves `existente.preco()` on the remaining entry.
- `desvincularInsumo`: same pattern as desvincularPeca.

**Gateway pattern for peça/insumo persistence** (implemented in `OrdemDeServicoDatabaseGateway`):
- `vincularOuSomarPeca/vincularOuSomarInsumo`: `findByOrdemServicoIdAnd{Peca|Insumo}Id` → `ifPresentOrElse(somarQuantidade, save new entity)`.
- `desvincularOuSubtrairPeca/desvincularOuSubtrairInsumo`: `findByOrdemServicoIdAnd{Peca|Insumo}Id` → `ifPresent(entity → if qty-delta≤0: delete(entity), else: diminuirQuantidade(...))`.

The join tables `ordem_servico_peca` and `ordem_servico_insumo` each have a `UNIQUE KEY` on `(ordem_servico_id, {peca|insumo}_id)`, so duplicate rows are prevented at the DB level.

**Use case flow for vincular** (same for peca and insumo):
1. Load `OrdemDeServico` → validate status via domain (`vincularPeca/vincularInsumo` throws if not `EM_DIAGNOSTICO`).
2. Load `Peca`/`Insumo` → call `baixarEstoque(quantidade)` (throws `EstoqueInsuficienteException` if insufficient).
3. `pecaGateway/insumoGateway.atualizar(peca/insumo)` to persist the stock change.
4. `ordemDeServicoGateway.vincularOuSomarPeca/vincularOuSomarInsumo(...)` to persist the link.

**Use case flow for desvincular** (same for peca and insumo):
1. Load `OrdemDeServico` → load `Peca`/`Insumo`.
2. `peca/insumo.devolverEstoque(quantidade)` to restore stock.
3. `ordemDeServico.desvincularPeca/desvincularInsumo(id, quantidade)` — domain validates status, presence, and quantity.
4. `pecaGateway/insumoGateway.atualizar(...)` and `ordemDeServicoGateway.desvincularOuSubtrairPeca/desvincularOuSubtrairInsumo(...)`.

**Use case flow for enviarOrcamento** (`POST /ordem-servico/orcamento/envio/{id}`, requires `ROLE_ATENDENTE`):
1. Load `OrdemDeServico` → call `gravarEnvioOrcamento()` which delegates to `state.enviarOrcamento(this)` (only succeeds from `DIAGNOSTICO_CONCLUIDO`).
2. `ordemDeServicoGateway.atualizar(ordemDeServico)` to persist `dataEnvioOrcamento` and new status.
3. `notificacaoGateway.enviarOrcamento(clienteId, orcamento.valorTotal())` to notify the client.

**Use case flow for aprovar/recusar orçamento** (`POST /ordem-servico/orcamento/aprovar|recusar/{id}`, requires `ROLE_ATENDENTE`):
1. Load `OrdemDeServico` → call `aprovar()` or `cancelar()` on domain (only from `AGUARDANDO_APROVACAO`).
2. `ordemDeServicoGateway.atualizar(ordemDeServico)` to persist the new status and date.

**Use case flow for iniciar/finalizar serviço** (`PATCH /ordem-servico/{id}/servicos/{servicoId}/iniciar|finalizar`, requires `ROLE_MECANICO`):
1. Resolve `mecanicoId` from token via `TokenGateway.getUserId()`.
2. Load `OrdemDeServico` → call `iniciarServico(servicoId)` or `finalizarServico(servicoId)`.
3. `ordemDeServicoGateway.atualizar(ordemDeServico)` to persist the updated `ServicoVinculado` list (and possible status change to `FINALIZADA`).

**Use case flow for entregar** (`PATCH /ordem-servico/{id}/entregar`, requires `ROLE_ATENDENTE`):
1. Load `OrdemDeServico` → call `entregar()` (only from `FINALIZADA`).
2. `ordemDeServicoGateway.atualizar(ordemDeServico)`.

### Estoque domain

`ItemEstoque` is an abstract base class for `Peca` and `Insumo`. It encapsulates stock quantity and exposes `baixarEstoque(qty)` (throws `EstoqueInsuficienteException` if insufficient) and `devolverEstoque(qty)`. Concrete subclasses call `ajustarQuantidade(qty)` (protected) from their `atualizar(...)` method.

`Peca.reconstituir` has 5 parameters `(id, nome, descricao, preco, quantidadeEstoque)` — no `UnidadeMedida`. `Insumo.reconstituir` has 6 parameters with `UnidadeMedida` as 5th.

### Security filter flow

`UserAuthenticationFilter` (active on all profiles except `controller-test`) intercepts every request:
1. If the endpoint is in `ENDPOINTS_SEM_AUTENTICACAO` → skip, pass through.
2. Otherwise → extract Bearer token from `Authorization` header → validate via `TokenGateway` → load user → set `SecurityContextHolder`.

`TokenGateway` (in `shared/seguranca/core/gateway/`) also exposes `getUserId()` — used by use cases that need the authenticated user's identity (e.g. `CriarOrdemDeServicoUseCase` to resolve the `Atendente`).

### Controller input validation pattern

Two validation patterns in use:
- **Field-level** (`@PlacaValida`): annotation + `ConstraintValidator`, `@Target(ElementType.FIELD)`. Validates old (`^[A-Z]{3}\\d{4}$`) and Mercosul (`^[A-Z]{3}\\d[A-Z]\\d{2}$`) plates after stripping `-`.
- **Class-level** (`@DocumentoValido`): annotation + `ConstraintValidator`, `@Target(ElementType.TYPE)` — used when validation spans multiple fields (e.g. exactly one of CPF/CNPJ must be present in `ClienteRequestJson`).

Standard field-level constraints: `@NotBlank`, `@NotNull`, `@CPF`, `@CNPJ` (Hibernate Validator), `@Min`/`@Max`.

## Test profiles and structure

| Profile | Purpose |
|---|---|
| `integration-test` | Full Spring context + Testcontainers MySQL. Requires Docker. |
| `controller-test` | `@WebMvcTest` slice — `UserAuthenticationFilter` is disabled via `NoSecurityConfiguration`. |

**Three test layers** (mirroring the package structure of the class under test):

| Layer | Base class / annotation | HTTP client |
|---|---|---|
| Unit | `@ExtendWith(MockitoExtension.class)` | — |
| Contract | `@WebMvcTest` + `@ActiveProfiles("controller-test")` + `@ImportAutoConfiguration(NoSecurityConfiguration.class)` | `MockMvc` |
| Integration | `@SpringBootTest(RANDOM_PORT)` + extends `AbstractContainer` | REST Assured (`RestAssured.port = port` in `@BeforeEach`) |

**`AbstractContainer`** starts a shared Testcontainers MySQL instance and truncates all tables (except `flyway_schema_history` and `roles`) both before and after each test. It uses `SET FOREIGN_KEY_CHECKS = 0/1` to allow truncation regardless of FK constraints.

**Integration test authentication pattern:** create a `UserEntity` with `securityConfiguration.passwordEncoder().encode(...)`, call `POST /authenticate/login` via REST Assured, extract the token, then pass it as `header("Authorization", "Bearer " + token)` on subsequent requests.

**Contract tests** must declare a `@MockitoBean` for every use case injected into the controller under test, even those not exercised in the test class — Spring context will fail to start otherwise.

**`ordemdeservico` integration test pattern:** instead of extending `AbstractContainer` directly, each feature area extends `AbstractOrdemDeServicoIntegrationTest` (in `ordemdeservico/infra/controller/`), which itself extends `AbstractContainer`. The abstract class holds all `@Autowired` repositories, `@LocalServerPort`, the `@BeforeEach` port setup, and shared helper methods (`obterToken(RoleEnum, email)`, `obterTokenAtendente`, `obterTokenMecanico`, `obterTokenOutroMecanico`, `criarClienteERetornarId`, `criarVeiculoERetornarId`, `criarOrdemERetornarId`, `criarOrdemEmDiagnosticoERetornarId`, `criarOrdemAguardandoAprovacaoERetornarId`, `criarServicoERetornarId`, `criarPecaERetornarId`, `criarInsumoERetornarId`). Use `obterToken(RoleEnum.ROLE_ADMINISTRADOR, "admin@test.com")` for admin-role tests. Each concrete test class covers one feature area (criação, diagnóstico, serviço, peça, insumo, envio de orçamento, aprovação, execução, entrega, detalhamento). DB-query helpers specific to a single class (e.g. `contarVinculosPecaNoBanco`) stay in that class, not in the abstract base.

> **REST Assured + Java 25 note:** REST Assured (Groovy-based) may throw `NullPointerException` in `applyProxySettings` on Java 25. If this occurs, add `--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED` to the `maven-surefire-plugin` `<argLine>`.
