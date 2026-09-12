# ADRs — Architecture Decision Records

Decisões arquiteturais permanentes do projeto Mecânica FIAP. Formato: contexto → decisão →
consequências. Uma decisão registrada aqui só muda com um novo ADR que a substitua.

| ADR | Decisão | Status |
|---|---|---|
| [0001](0001-monolito-modular-clean-architecture.md) | Monólito modular com Clean Architecture | Aceito |
| [0002](0002-comunicacao-sincrona-http.md) | Comunicação síncrona por HTTP/REST, sem broker de mensagens | Aceito |
| [0003](0003-escalabilidade-hpa.md) | Escalabilidade horizontal com HPA (CPU 70%, 1–4 réplicas) | Aceito |
| [0004](0004-infra-quatro-repositorios.md) | Infraestrutura em 4 repositórios Terraform com states independentes | Aceito |
| [0005](0005-state-pattern-ordem-servico.md) | State pattern no ciclo de vida da ordem de serviço | Aceito |
| [0006](0006-jwt-hs256-segredo-compartilhado.md) | JWT HS256 stateless com segredo compartilhado; autorização na aplicação | Aceito |
