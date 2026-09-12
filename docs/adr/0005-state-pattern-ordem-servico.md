# ADR-0005 — State pattern no ciclo de vida da ordem de serviço


## Contexto

A ordem de serviço tem um ciclo de vida com transições estritas:

```
RECEBIDA → EM_DIAGNOSTICO → DIAGNOSTICO_CONCLUIDO → AGUARDANDO_APROVACAO
   → (aprovar) EM_EXECUCAO → FINALIZADA → ENTREGUE
   → (cancelar) CANCELADA
```

Cada estado permite um conjunto diferente de operações (só se pode vincular peça em `RECEBIDA`/
`EM_DIAGNOSTICO`; só se pode iniciar serviço em `EM_EXECUCAO`; etc.), e uma transição inválida
deve ser rejeitada como regra de negócio, não como erro genérico.

## Decisão

Modelar o ciclo de vida com o **State pattern** dentro do domínio:

- `OrdemDeServico` guarda um `state: OrdemDeServicoState` (interface). Cada estado concreto
  (`OrdemDeServicoRecebidaState`, `OrdemDeServicoEmDiagnosticoState`, …) implementa **só** as
  transições que permite; o padrão da interface lança `TransicaoDeStatusInvalidaException`.
- As transições mutam o agregado por *setters* de pacote que só as classes de estado enxergam
  (`setDataInicioDiagnostico`, `setMecanicoId`, …).
- `OrdemDeServicoStateFactory.from(status)` reconstrói o objeto de estado correto a partir do
  enum `StatusOrdemDeServico` persistido, dentro do `reconstituir(...)`.
- O status persiste como `varchar` na coluna `ordem_servico.status`; o objeto de estado é
  comportamento em memória, não é persistido.

## Consequências

**Positivas**
- Cada regra de transição fica num único lugar; adicionar um estado é adicionar uma classe, não
  espalhar `if`/`switch`.
- Transição inválida vira exceção de domínio tipada, mapeada para HTTP 4xx pelo
  `GlobalExceptionHandler`.
- O domínio continua Java puro (alinhado com [ADR-0001](0001-monolito-modular-clean-architecture.md)).

**Negativas / trade-offs**
- Mais classes (uma por estado) e *setters* de visibilidade de pacote, que é um acoplamento
  deliberado entre o agregado e suas classes de estado.
- `reconstituir(...)` usa um construtor privado sem argumentos e atribui campos direto
  (contornando o construtor de criação) — dívida técnica conhecida e marcada no código.

## Alternativas consideradas

- **`switch`/`enum` com métodos** — concentra as regras no enum; fica pesado conforme o número de
  transições e operações condicionadas ao estado cresce.
- **Máquina de estados declarativa (ex.: Spring Statemachine)** — traria dependência de framework
  ao domínio, violando o ADR-0001, para um ciclo de vida pequeno e fixo.
