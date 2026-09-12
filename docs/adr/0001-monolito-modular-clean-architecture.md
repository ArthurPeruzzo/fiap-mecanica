# ADR-0001 — Monólito modular com Clean Architecture

## Contexto

A aplicação cobre um único processo de negócio coeso (o ciclo de uma ordem de serviço, com
cadastros e estoque de apoio). É um projeto acadêmico, com uma equipe pequena e sem requisito de
implantação independente de partes do sistema. Ao mesmo tempo, as regras de negócio (máquina de
estados da OS, cálculo de orçamento, baixa/devolução de estoque) precisam ser testáveis e
resistentes a mudanças de framework, banco e forma de exposição (HTTP hoje).

## Decisão

Construir a aplicação como um **monólito modular** organizado por **Clean Architecture**:

- Módulos de negócio por subdomínio: `gestao`, `estoque`, `ordemdeservico`, mais `shared`.
- Cada módulo dividido em `core/` (domínio) e `infra/` (framework):
  - `core/` — entidades de domínio, *use cases*, DTOs e **interfaces de gateway**, em Java puro,
    **sem nenhuma dependência de Spring, JPA ou HTTP**. Use cases são instanciados com `new`
    dentro dos *Clean Controllers*, não são beans.
  - `infra/` — controllers HTTP, implementações dos gateways (JPA), entidades de banco,
    segurança, serialização. Depende do `core`, nunca o contrário.
- Saída dos use cases pelo padrão *output port* (presenter), não por retorno direto.
- Um par de controllers por área: `XxxHttpController` (bean Spring, mapeamento HTTP) +
  `XxxCleanController` (Java puro, orquestra use cases).

## Consequências

**Positivas**
- Regras de negócio isoláveis e testáveis sem subir Spring nem banco (testes unitários com
  Mockito; contrato com `@WebMvcTest`; integração com Testcontainers).
- Trocar de banco ou de mecanismo de entrega afeta só `infra/`.
- Deploy, build e transação simples — um artefato, um banco.

**Negativas / trade-offs**
- Mais boilerplate: cada caso de uso tem controller-par, gateway-interface + implementação,
  presenter.
- Disciplina manual — nada impede tecnicamente um `import` de `infra` dentro de `core`; depende
  de revisão.
- Escala como uma unidade só; não há isolamento de falha entre módulos.

## Alternativas consideradas

- **Camadas tradicionais (controller → service → repository)** — menos código, mas acopla o
  domínio ao Spring/JPA e dificulta testar regra sem infraestrutura.
- **Microsserviços por subdomínio** — desnecessário para o tamanho do problema; adicionaria
  rede, consistência distribuída e operação de vários deploys sem ganho real neste contexto.
