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

| CPF | Role |
|---|---|
| `22255588846` | `ROLE_ADMINISTRADOR` |
| `33366699957` | `ROLE_ATENDENTE` |
| `11144477735` | `ROLE_MECANICO` |

Login via `POST /authenticate/login`. Migration `V14` seeds sample data (clients, vehicles, parts, supplies, services, orders in every status) for immediate API exploration. Swagger UI: `http://localhost:8080/swagger-ui.html`.

## Environment variables

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:mysql://127.0.0.1:3306/mecanica` | JDBC URL |
| `DB_USERNAME` | `root` | DB user |
| `DB_PASSWORD` | `root` | DB password |
| `JWT_SECRET` | — | Base64URL-encoded secret (min 32 bytes decoded). Required in production. |
| `url.aprovar.orcamento` | `http://localhost:8080/ordem-servico/orcamento/externo/aprovar/` | Base URL appended with the token for the client approval link |
| `url.recusar.orcamento` | `http://localhost:8080/ordem-servico/orcamento/externo/recusar/` | Base URL appended with the token for the client rejection link |

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
│       ├── controller/     # Cliente{Clean|Http}Controller, Veiculo{Clean|Http}Controller
│       │                   # validation/: @DocumentoValido (class-level), @PlacaValida (field-level)
│       │                   # presenter/: ListarClientes, ListarVeiculos presenters
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
│   └── infra/              # Peca{Clean|Http}Controller, Insumo{Clean|Http}Controller,
│                           # presenter/: ListarPecas, ListarInsumos presenters,
│                           # entities, repositories
└── ordemdeservico/         # Business module: service orders and services
    ├── core/
    │   ├── domain/
    │   │   ├── ordemdeservico/ # OrdemDeServico + state machine, ServicoVinculado, PecaVinculada,
    │   │   │                   # InsumoVinculado records, Orcamento record, LinkAprovacaoOrcamento
    │   │   │   └── mensagem/   # MensagemOrdemDeServicoRecebidaFactory, MensagemOrcamentoEnviadoFactory,
    │   │   │                   # MensagemOrcamentoAprovadoFactory, MensagemOrdemCanceladaFactory,
    │   │   │                   # MensagemOrdemFinalizadaFactory, MensagemOrdemEntregueFactory
    │   │   └── servico/        # Servico
    │   ├── dto/            # CriarOrdemDeServicoDto, OrdemDeServicoListagemDto,
    │   │               # ServicoVinculadoDto, PecaVinculadaDto, InsumoVinculadoDto,
    │   │               # Criar/Atualizar/ListarServicoDto
    │   ├── gateway/        # OrdemDeServicoGateway, ServicoGateway, LinkAprovacaoOrcamentoGateway
    │   ├── usecase/
    │   │   ├── ordemdeservico/ # CriarOrdemDeServico, IniciarDiagnostico, ConcluirDiagnostico,
    │   │   │                   # VincularServico, DesvincularServico,
    │   │   │                   # VincularPeca, DesvincularPeca, VincularInsumo, DesvincularInsumo,
    │   │   │                   # EnviarOrcamentoOrdemDeServico,
    │   │   │                   # OrcamentoAprovadoOrdemDeServico (abstract base),
    │   │   │                   #   OrcamentoAprovadoViaAtendenteUseCase, OrcamentoAprovadoViaTokenUseCase,
    │   │   │                   # OrcamentoRecusadoOrdemDeServico (abstract base),
    │   │   │                   #   OrcamentoRecusadoViaAtendenteUseCase, OrcamentoRecusadoViaTokenUseCase,
    │   │   │                   # IniciarServicoOrdemDeServico, FinalizarServicoOrdemDeServico,
    │   │   │                   # EntregarOrdemDeServico, ListarOrdemDeServico,
    │   │   │                   # ConsultarStatusOrdemDeServico
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
        ├── controller/     # Five Clean+Http pairs: OrdemDeServico (CRUD/listar/status/entregar),
        │                   # DiagnosticoOrdemDeServico (iniciar/concluir diagnóstico),
        │                   # ExecucaoServico (iniciar/finalizar serviço), Servico (CRUD serviços),
        │                   # VinculoOrdemDeServico (vincular/desvincular serviço/peça/insumo),
        │                   # Orcamento (enviar, aprovar/recusar via atendente ou token)
        │                   # json/: request JSONs including VincularPecaRequestJson,
        │                   #        DesvincularPecaRequestJson, VincularInsumoRequestJson,
        │                   #        DesvincularInsumoRequestJson (all carry Integer quantidade)
        │                   # presenter/: CriarOrdemDeServico, ListarOrdemDeServico,
        │                   #             ConsultarStatusOrdemDeServico, ListarServicos presenters
        └── gateway/
            ├── database/   # OrdemDeServicoDatabaseGateway
            ├── entity/     # OrdemDeServicoEntity, ServicoEntity,
            │               # OrdemDeServicoServicoEntity (join: ordem_servico_servico — stores preco, StatusServico, timestamps),
            │               # OrdemDeServicoPecaEntity, OrdemDeServicoInsumoEntity,
            │               # LinkAprovacaoOrcamentoEntity
            └── repository/ # OrdemDeServicoRepository, ServicoRepository,
                            # OrdemDeServicoServicoRepository,
                            # OrdemDeServicoPecaRepository, OrdemDeServicoInsumoRepository,
                            # LinkAprovacaoOrcamentoRepository
```

`shared/seguranca/` follows the same core/infra split:
- `core/`: `User` (identified by `Cpf`, from `shared/valueobjects/`), `Role`, `RoleEnum`, `Password`/`PasswordHash` value objects, `UserGateway`, `TokenGateway`, `AuthenticateUserUseCase`, domain exceptions
- `infra/`: `SecurityConfiguration`, `UserAuthenticationFilter`, `JwtTokenService`, `AuthenticateController`, JPA entities/repositories

`shared/notificacao/core/domain/` contains: `Mensagem` (record: `clienteId`, `conteudo`), abstract `MensagemFactory` (`criar(MensagemParams params)`), and `MensagemParams` (builder: `clienteId`, `ordemId`, `valorTotal`, `token`, `urlAprovar`, `urlRecusar`). `NotificacaoGateway` exposes a single method `enviar(Mensagem mensagem)`; the only implementation is `NotificacaoMockGateway`, which logs via SLF4J. Concrete factories live in `ordemdeservico/core/domain/ordemdeservico/mensagem/` and extend `MensagemFactory` — one per notification event (OS recebida, orçamento enviado, orçamento aprovado, OS cancelada, OS finalizada, OS entregue). Usage pattern: `notificacaoGateway.enviar(new MensagemXxxFactory().criar(params))`.

### Controller layer: CleanController + HttpController pair

Every feature area has **two** controller classes:

- **`XxxCleanController`** — plain Java, no Spring annotations. Instantiates use cases via `new`, receives all required gateways via its own constructor. Orchestrates use case calls and manages presenters. Not a Spring bean.
- **`XxxHttpController`** — `@RestController` Spring bean. Handles HTTP mapping, request/response JSON, validation annotations, and OpenAPI (`@Tag`, `@Operation`). Instantiates the `CleanController` in its own constructor by passing through gateway dependencies injected by Spring.

The `ordemdeservico` module is split into five controller pairs: `OrdemDeServico`, `DiagnosticoOrdemDeServico`, `ExecucaoServico`, `VinculoOrdemDeServico`, and `Orcamento`.

### Presenter / OutputPort pattern

Use cases that produce output use the **output port** (presenter) pattern instead of returning values directly:

1. **`ListarXxxOutputPort`** interface (in `core/usecase/`) declares `apresentar(Pagina<Domain>)` or similar.
2. **`XxxPresenter`** class (in `infra/controller/presenter/`) implements the interface, stores a view model, and maps domain objects to response JSONs.
3. The presenter is instantiated inside the `CleanController` method, passed to the use case constructor, and then queried for the view model after execution.

Use cases that don't produce output (create/update/delete) return `void` — no presenter needed.

### Key architectural rules

- **`core/` has zero Spring/JPA dependencies.** Use cases are plain Java — no `@Service` or any other Spring annotation. They are instantiated with `new` inside `CleanController` methods, not managed by Spring. Domain objects are plain Java. JPA entities live exclusively in `infra/gateway/entity/`.
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
- **Roles** are seeded by Flyway (`V1__create_security_struct.sql`): `ROLE_ATENDENTE`, `ROLE_MECANICO`, `ROLE_ADMINISTRADOR`. The `SecurityConfiguration` role constants must match `RoleEnum`. `ROLE_ADMINISTRADOR` controls `/cliente/**`, `/veiculo/**`, `/peca/**`, `/insumo/**`, `/servico/**`, and `GET /ordem-servico/detalhamento`. `ROLE_ATENDENTE` controls `POST /ordem-servico`, `POST /ordem-servico/orcamento/**`, and `PATCH /ordem-servico/*/entregar`. `ROLE_MECANICO` controls all `PATCH /ordem-servico/*/diagnostico*`, vincular/desvincular serviço/peça/insumo, and iniciar/finalizar serviço endpoints. `GET /ordem-servico/{id}/status` requires any authenticated role (`hasAnyRole(ATENDENTE, MECANICO, ADMINISTRADOR)`). `GET /ordem-servico/orcamento/externo/**` is in `ENDPOINTS_SEM_AUTENTICACAO` (public, no auth). Any other unmatched request is denied (`anyRequest().denyAll()`).

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
3. Create `LinkAprovacaoOrcamento(ordemServicoId)` — generates a UUID token, sets expiry 3 days out — and persist via `linkAprovacaoOrcamentoGateway.salvar(link)`.
4. Build `MensagemParams` (clienteId, ordemId, valorTotal, token, urlAprovar, urlRecusar) and call `notificacaoGateway.enviar(new MensagemOrcamentoEnviadoFactory().criar(params))`.

**`LinkAprovacaoOrcamento`** domain object: `estaValido()` returns true when `dataUtilizacao == null` and now is before `dataExpiracao`. `marcarComoUtilizado()` sets `dataUtilizacao = now()`. Tokens are single-use and expire after 3 days.

**Use case flow for aprovar/recusar orçamento via atendente** (`POST /ordem-servico/orcamento/aprovar|recusar/{id}`, requires `ROLE_ATENDENTE`):
1. Load `OrdemDeServico` by id → call `aprovar()` or `cancelar()` on domain (only from `AGUARDANDO_APROVACAO`).
2. `ordemDeServicoGateway.atualizar(ordemDeServico)` to persist the new status and date.
3. `notificacaoGateway.enviar(new MensagemOrcamentoAprovadoFactory()|MensagemOrdemCanceladaFactory().criar(params))`.

**Use case flow for aprovar/recusar orçamento via token** (`GET /ordem-servico/orcamento/externo/aprovar|recusar/{token}`, public — no auth):
1. Load `LinkAprovacaoOrcamento` by token → validate `estaValido()` (throws `LinkAprovacaoOrcamentoInvalidoException` if expired/used).
2. Load `OrdemDeServico` by `link.ordemDeServicoId` → call `aprovar()` or `cancelar()`.
3. `ordemDeServicoGateway.atualizar(...)` and `link.marcarComoUtilizado()` + `linkAprovacaoOrcamentoGateway.atualizar(link)`.
4. Notify client via `notificacaoGateway.enviar(...)`.

Both flows share a common abstract base (`OrcamentoAprovadoOrdemDeServicoUseCase`, `OrcamentoRecusadoOrdemDeServicoUseCase`) that handles domain mutation, persistence, and notification. The atendente and token subclasses only differ in how they resolve the `OrdemDeServico` (by id vs. by link token).

**Use case flow for iniciar/finalizar serviço** (`PATCH /ordem-servico/{id}/servicos/{servicoId}/iniciar|finalizar`, requires `ROLE_MECANICO`):
1. Resolve `mecanicoId` from token via `TokenGateway.getUserId()`.
2. Load `OrdemDeServico` → call `iniciarServico(servicoId)` or `finalizarServico(servicoId)`.
3. `ordemDeServicoGateway.atualizar(ordemDeServico)` to persist the updated `ServicoVinculado` list (and possible status change to `FINALIZADA`).

**Use case flow for entregar** (`PATCH /ordem-servico/{id}/entregar`, requires `ROLE_ATENDENTE`):
1. Load `OrdemDeServico` → call `entregar()` (only from `FINALIZADA`).
2. `ordemDeServicoGateway.atualizar(ordemDeServico)`.

**`GET /ordem-servico/{id}/status`** (requires any authenticated role): delegates to `ConsultarStatusOrdemDeServicoUseCase`, which returns a `ConsultarStatusOrdemDeServicoDto(id, status)` — a lightweight status check without loading the full order graph.

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

**`ordemdeservico` integration test pattern:** instead of extending `AbstractContainer` directly, each feature area extends `AbstractOrdemDeServicoIntegrationTest` (in `ordemdeservico/infra/controller/`), which itself extends `AbstractContainer`. The abstract class holds all `@Autowired` repositories (including `linkAprovacaoOrcamentoRepository`), `@LocalServerPort`, the `@BeforeEach` port setup, and shared helper methods (`obterToken(RoleEnum, cpf)`, `obterTokenAtendente`, `obterTokenMecanico`, `obterTokenOutroMecanico`, `criarClienteERetornarId`, `criarVeiculoERetornarId`, `criarOrdemERetornarId`, `criarOrdemEmDiagnosticoERetornarId`, `criarOrdemAguardandoAprovacaoERetornarId`, `criarServicoERetornarId`, `criarPecaERetornarId`, `criarInsumoERetornarId`). Use `obterToken(RoleEnum.ROLE_ADMINISTRADOR, "44477711107")` for admin-role tests. Each concrete test class covers one feature area (criação, diagnóstico, serviço, peça, insumo, envio de orçamento, aprovação, execução, entrega, detalhamento). DB-query helpers specific to a single class (e.g. `contarVinculosPecaNoBanco`) stay in that class, not in the abstract base.

> **REST Assured + Java 25 note:** REST Assured (Groovy-based) may throw `NullPointerException` in `applyProxySettings` on Java 25. If this occurs, add `--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED` to the `maven-surefire-plugin` `<argLine>`.
