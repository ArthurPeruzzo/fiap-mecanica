# Documentação da Arquitetura — Fase 3

Índice da documentação arquitetural do projeto Mecânica FIAP.

| Documento | Conteúdo |
|---|---|
| [`arquitetura.md`](arquitetura.md) | Diagrama de componentes (visão de nuvem: APIs, banco, monitoramento) e diagramas de sequência (autenticação; abertura de ordem de serviço) |
| [`banco-de-dados.md`](banco-de-dados.md) | Justificativa formal da escolha do banco, ajustes no modelo relacional, diagrama ER e explicação dos relacionamentos |
| [`rfc/`](rfc/) | RFCs — decisões técnicas relevantes (escolha da nuvem, do banco, da estratégia de autenticação) |
| [`adr/`](adr/) | ADRs — decisões arquiteturais permanentes (Clean Architecture, comunicação síncrona, HPA, split em 4 repositórios, State pattern da OS, JWT HS256 compartilhado) |

Visão geral de runtime, autenticação, observabilidade e deploy: [`../README.md`](../README.md).
