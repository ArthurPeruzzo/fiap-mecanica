# ADR-0006 — JWT HS256 stateless com segredo compartilhado; autorização na aplicação


## Contexto

Há dois emissores de token: a própria aplicação (`POST /authenticate/login`, funcionário por
CPF+senha) e a **Lambda de autenticação** (`POST /auth/cliente`, cliente por CPF). O token
emitido por qualquer um dos dois precisa ser aceito pelo mesmo filtro de segurança da aplicação.
O API Gateway (HTTP API v2) tem um *authorizer* JWT nativo, mas ele só valida OIDC/JWKS (RS256).

## Decisão

- **JWT stateless, assinado com HS256** (segredo simétrico). Claims: `iss = mecanica-fiap`,
  `sub = <id do usuário>`, `exp = agora + 4h`. O CPF **não** vai nos claims.
- O **mesmo `JWT_SECRET`** (Base64URL, ≥ 32 bytes decodificados) é configurado na aplicação e na
  Lambda — como GitHub Secret em ambos os repositórios. A Lambda assina; a aplicação valida.
- **Validação e autorização ficam 100% na aplicação**: `UserAuthenticationFilter` valida
  assinatura/`iss`/`exp` e carrega o usuário; `SecurityConfiguration` faz o controle de acesso
  por rota e perfil (`hasRole(...)`, `anyRequest().denyAll()`).
- O **API Gateway só roteia** (`HTTP_PROXY`), repassando o header `Authorization` intacto. Não
  usa *authorizer*.
- Cliente não tem senha: a Lambda chama `GET /authenticate/cliente/status` e a aplicação
  provisiona um `User` `ROLE_CLIENTE` sob demanda (a existência/criação do cliente é regra de
  negócio e mora na aplicação, não na Lambda).

## Consequências

**Positivas**
- Um único ponto de verdade para autorização (a aplicação); o gateway não precisa conhecer
  perfis nem rotas.
- Stateless — qualquer réplica valida o token sem sessão compartilhada (alinha com o
  [ADR-0003](0003-escalabilidade-hpa.md)).
- Emissor duplo (app + Lambda) sem duplicar regra de negócio.

**Negativas / trade-offs**
- **Segredo compartilhado**: se o `JWT_SECRET` divergir entre app e Lambda, o token é emitido
  mas rejeitado depois (401) — sintoma que não aponta a causa. Rotacionar exige atualizar os
  dois repositórios juntos.
- HS256 é simétrico: quem valida também poderia assinar. Aceitável aqui (só dois componentes
  confiáveis), mas não escala para muitos consumidores.
- `GET /authenticate/cliente/status` é público e tem efeito colateral (cria linha em `users`),
  e o CPF trafega em *query string* — riscos conscientemente aceitos (detalhados no RFC de
  autenticação).

## Alternativas consideradas

- **Authorizer JWT nativo do API Gateway** — exigiria RS256 + JWKS; incompatível com o contrato
  HS256 de segredo compartilhado. Migrar para RS256 seria hospedar um JWKS e um par de chaves,
  desproporcional ao escopo.
- **Lambda authorizer tipo REQUEST** — moveria a autorização para fora da aplicação, duplicando
  a lógica de perfis; rejeitado.
- **Sessão server-side** — quebra o stateless e o escalonamento por réplica.
