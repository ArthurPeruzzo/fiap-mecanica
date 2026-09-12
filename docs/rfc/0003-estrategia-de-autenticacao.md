# RFC-0003 — Estratégia de autenticação: JWT + Function Lambda por CPF

- **Relacionado:** [ADR-0006](../adr/0006-jwt-hs256-segredo-compartilhado.md), [ADR-0002](../adr/0002-comunicacao-sincrona-http.md)

## Resumo

Dois atores autenticam de formas diferentes: **funcionário** por CPF + senha direto na
aplicação; **cliente** por CPF através de uma **Function Lambda** exposta no API Gateway. Ambos
recebem um **JWT HS256** stateless, aceito pelo mesmo filtro da aplicação. A Lambda valida o
formato do CPF e assina o token, mas **delega à aplicação** a consulta e o provisionamento do
cliente na base.

## Contexto e motivação

- O enunciado exige uma Function serverless que: valide o CPF do cliente, consulte a existência
  e o status do cliente na base de dados e devolva um JWT válido para as APIs protegidas.
- O cliente **não tem senha** — só o CPF.
- O API Gateway (HTTP API v2) já está na frente da aplicação; ele tem um *authorizer* JWT
  nativo, mas só aceita OIDC/JWKS (RS256).
- A regra "existe cliente para este CPF? se sim, garanta um usuário com `ROLE_CLIENTE`" é regra
  de negócio e já vive na aplicação.

## Opções consideradas

### A — Lambda assina o token; a aplicação faz a consulta do cliente (recomendada)

A Lambda valida o CPF (formato + dígitos) e assina o JWT. Para "existe/está apto", chama
`GET /authenticate/cliente/status?cpf=...` na aplicação, que confirma o cliente na base e cria um
`User` `ROLE_CLIENTE` na primeira vez.

**Prós:** a regra de negócio fica num lugar só (a aplicação); a Lambda continua fina e sem
acesso ao banco nem à VPC; atende o enunciado (o status do cliente **é** consultado na base,
ainda que pela aplicação).
**Contras:** acoplamento síncrono — se a aplicação está fora do ar, a autenticação de cliente
falha na hora (`502`/`504`); a consulta ao banco é indireta.

### B — Lambda consulta o RDS diretamente

**Contras:** exigiria `vpc_config` na Lambda + rota para o banco (NAT ou banco em subnet
pública), e **duplicaria** a lógica de existência/criação do `User` cliente fora da aplicação,
com a Lambda conhecendo o schema. Rejeitada.

### C — Sem Lambda, autenticação só na aplicação

**Contras:** não atende o requisito explícito de uma Function serverless para autenticação.

### D — Authorizer JWT nativo do API Gateway

**Contras:** só valida RS256/JWKS; incompatível com o contrato HS256 de segredo compartilhado.
Migrar para RS256 exigiria hospedar um JWKS e um par de chaves — desproporcional. Ver
[ADR-0006](../adr/0006-jwt-hs256-segredo-compartilhado.md).

## Recomendação

**Opção A.** Token JWT HS256, `iss = mecanica-fiap`, `sub = <id do usuário>`, `exp = agora + 4h`,
CPF fora dos claims. O mesmo `JWT_SECRET` é configurado na aplicação e na Lambda. Validação e
autorização por rota/perfil ficam 100% na aplicação; o gateway só roteia e repassa o header
`Authorization`.

## Riscos conscientemente aceitos

Decisões deliberadas para o escopo acadêmico, documentadas aqui para não parecerem descuido. O
caminho de mitigação está descrito, mas não foi implementado.

| # | Risco | Mitigação não adotada |
|---|---|---|
| A1 | `GET /authenticate/cliente/status` é **público, GET e com efeito colateral** (cria linha em `users`). Permite enumerar quais CPFs são clientes (oráculo de existência) e provisionar `User` anonimamente, sem limite. `GET` com efeito colateral quebra a semântica HTTP (retry/prefetch repetem a escrita). | Virar `POST` com o CPF no corpo + header com segredo compartilhado entre Lambda e aplicação. |
| A2 | O **CPF trafega em *query string*** (`?cpf=`) → cai em access log do ELB, do API Gateway e do Spring. Dado pessoal (LGPD). | Resolvido junto com A1 ao migrar para `POST` com o CPF no corpo. |
| A3 | O `User` criado para o cliente recebe uma **senha aleatória não hasheada**. | Sem impacto real: esse `User` nunca autentica por senha (o único caminho para o token de cliente é a Lambda). Hashear exigiria injetar `PasswordEncoder` (Spring) num use case de `core/`. |

## Trade-offs e consequências

- **Segredo compartilhado** (`JWT_SECRET` idêntico em dois repositórios): se divergir, o token é
  emitido mas rejeitado depois (401) — sintoma que não aponta a causa. Rotação exige atualizar
  os dois repositórios juntos. Detalhe em [ADR-0006](../adr/0006-jwt-hs256-segredo-compartilhado.md).
- **Cadeia de timeouts** (`HTTP 15s < Lambda 25s < API Gateway 30s`): o HTTP tem que estourar
  primeiro para a Lambda responder `504` em vez de ser morta pelo runtime.
- **A rota `POST /auth/cliente`** no API Gateway só é criada quando a Lambda já existe (guarda
  por `count` no Terraform).
- Stateless — qualquer réplica valida o token sem sessão compartilhada
  ([ADR-0003](../adr/0003-escalabilidade-hpa.md)).
