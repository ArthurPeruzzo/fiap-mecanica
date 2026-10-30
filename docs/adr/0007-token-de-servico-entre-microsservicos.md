# ADR-0007 — Token de serviço (JWT HS256 de segredo próprio) na chamada do monólito ao estoque

## Contexto

Com a extração do estoque para o microsserviço `fiap-estoque`, o monólito passou a chamar o estoque
por HTTP em duas famílias de rota internas:

- `/reserva/**` — criar/atualizar, confirmar e liberar reservas por ordem de serviço;
- `/catalogo/**` — consulta de peças e insumos (preço, nome, disponibilidade).

Essas rotas não são usadas por pessoas: quem chama é o `fiap-mecanica`. Mesmo assim, elas
nasceram em `permitAll`: só `/peca/**` e `/insumo/**` exigiam o JWT de administrador. Isso deixava
a proteção das rotas internas dependente apenas de o estoque não estar exposto na rede, o que
contradiz a premissa de *zero trust* (nenhuma chamada é confiável só por vir de dentro do cluster).

O JWT de usuário ([ADR-0006](0006-jwt-hs256-segredo-compartilhado.md)) não serve para isso: ele
representa uma pessoa (`sub` = id do usuário, `roles`), é emitido no login e vale 4 h. Repassá-lo
ao estoque misturaria "quem é o usuário" com "qual sistema está chamando", e uma reserva feita pelo
fluxo do token externo de aprovação (sem usuário logado) não teria token nenhum para repassar.

## Decisão

O `fiap-mecanica` gera **um token de serviço a cada chamada** ao estoque, e o estoque o valida nas
rotas internas.

- **Formato:** JWT HS256 com **segredo próprio** (`SERVICE_JWT_SECRET`, Base64URL, ≥ 32 bytes),
  **diferente do `JWT_SECRET` de usuário**. Quem conhece um dos segredos não consegue forjar o
  outro tipo de token.
- **Claims:**

  | Claim | Valor | Validado no estoque |
  |---|---|---|
  | `iss` | `mecanica-fiap-servicos` | sim |
  | `sub` | `svc:fiap-mecanica` (quem chama) | não é usado para autorizar |
  | `aud` | `fiap-estoque` (quem deve aceitar) | sim |
  | `scope` | permissões separadas por espaço | sim (autorização da rota) |
  | `iat` / `exp` | agora / agora + 90 s | `exp` sim, com tolerância (*leeway*) de 10 s |
  | `jti` | UUID | não (ver limitações) |

- **Escopos mínimos por rota:** `/catalogo/**` exige `catalogo:leitura`; `/reserva/**` exige
  `reserva:escrita`. O monólito escolhe o escopo pelo caminho da requisição, então uma chamada de
  catálogo nunca carrega o poder de escrever reservas.
- **Sem cache e sem servidor de autenticação:** o token é montado e assinado localmente em
  memória (microssegundos) por um `ClientHttpRequestInterceptor` do `RestClient`. Como cada
  token vive 90 s e nasce na própria chamada, não há renovação, expiração em uso nem estado
  para guardar.
- **Dois filtros, sem ambiguidade:** no estoque, `JwtAuthenticationFilter` (usuário) ignora
  `/reserva/**` e `/catalogo/**`; `ServicoAuthenticationFilter` só atua nelas. Um JWT de usuário
  não abre rota interna, e um token de serviço não abre `/peca/**` ou `/insumo/**`.
- **Falha fechada:** sem `SERVICE_JWT_SECRET` o estoque (e o monólito) não sobem, como já ocorre
  com o `JWT_SECRET`.

Respostas do estoque: **401** para token ausente, malformado, expirado, de outra audiência ou
assinado com outro segredo; **403** para token válido sem o escopo da rota.

## Consequências

**Positivas**
- Cada chamada entre serviços é autenticada e autorizada, sem depender da topologia de rede.
- Princípio do menor privilégio: escopo por rota, audiência fixa e vida de 90 s limitam o dano de
  um token vazado em log ou captura de tráfego.
- Custo de implementação baixo: a mesma biblioteca JWT já usada, nenhum componente novo.
- A identidade do chamador (`sub`) e o `jti` já estão no formato esperado por uma evolução futura
  (novos serviços, chaves assimétricas) sem mudar o contrato.
- Cobertura automatizada nos dois lados (escopo trocado, audiência errada, expirado, assinado com
  o segredo de usuário, token de serviço em rota de usuário e o inverso) e validação manual contra
  os processos reais.

**Negativas / trade-offs**
- **Segredo simétrico compartilhado:** o estoque, que só deveria validar, também tem o poder de
  assinar tokens de serviço. Aceitável com dois componentes confiáveis; não escala para muitos
  consumidores (mesmo trade-off do ADR-0006).
- **Sem proteção contra replay:** o `jti` é emitido, mas o estoque não o registra. Dentro dos 90 s
  (mais o *leeway*), um token capturado pode ser reutilizado. A idempotência de `confirmar` e
  `liberar` e o PUT absoluto das reservas reduzem o efeito, mas não o eliminam. Exigir `jti` único
  pediria um cache compartilhado entre réplicas e foi considerado desproporcional.
- **Relógios:** o monólito e o estoque precisam de horários próximos. O *leeway* de 10 s absorve
  pequenas diferenças; um desvio maior produz 401 sem causa evidente.
- **Rotação do segredo** exige atualizar os dois serviços juntos (GitHub Secret do monólito,
  Secret do cluster, `.env` local). Não há suporte a dois segredos válidos ao mesmo tempo.
- **Segredo operacional novo:** o `cd.yml` do monólito referencia o GitHub Secret
  `SERVICE_JWT_SECRET`; sem ele o deploy falha. O repositório do estoque não tem pipeline de
  deploy próprio.
- Os testes de rota interna passaram a depender do token de serviço de teste.

## Alternativas consideradas

- **Repassar o JWT do usuário ao estoque** — acopla autorização de usuário à comunicação entre
  sistemas, não cobre o fluxo do token externo de aprovação e dá ao estoque um token de 4 h com
  os perfis da pessoa.
- **Mesmo `JWT_SECRET` para os dois tipos de token** — qualquer um que forjasse um token de
  usuário forjaria também um de serviço, e o contrário. Rejeitado em favor de segredo separado.
- **API key estática no header** — sem expiração, sem escopo, sem audiência; um vazamento vale
  até alguém notar e trocar.
- **Servidor de autorização (OAuth2 *client credentials*, ex.: Keycloak/Cognito)** — é o padrão de
  mercado para muitos serviços, mas adiciona um componente, um ponto de falha e um salto de rede a
  cada chamada; desproporcional para dois serviços neste escopo.
- **Chaves assimétricas (RS256/ES256) com JWKS** — o estoque passaria a só validar, com a chave
  privada apenas no monólito. É o caminho natural quando houver mais serviços emissores ou
  consumidores; adiado por exigir hospedar chaves e rotacioná-las.
- **mTLS (malha de serviço ou certificados por pod)** — autentica a conexão e dispensa o token,
  mas exige infraestrutura (CA, malha) fora do escopo do projeto. Pode coexistir com o token.
- **Apenas isolamento de rede (ClusterIP/NetworkPolicy)** — continua sendo útil como defesa em
  profundidade, mas sozinho é a premissa que esta decisão substitui.

## Evolução prevista

1. Se surgir um terceiro serviço, trocar para assinatura assimétrica (o contrato de claims não
   muda) ou adotar mTLS.
2. Se o replay passar a importar, registrar o `jti` em cache compartilhado até o `exp`.
3. Ao introduzir mensageria (Saga), o consumidor/produtor precisa de identidade equivalente; este
   ADR cobre apenas as chamadas HTTP síncronas.
