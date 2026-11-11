# Documentação da Arquitetura — Fases 3 e 4

Índice da documentação arquitetural do projeto Mecânica FIAP.

| Documento | Conteúdo |
|---|---|
| [`arquitetura.md`](arquitetura.md) | Diagrama de componentes (Fase 3: nuvem, APIs, banco, monitoramento), diagramas de sequência (autenticação; abertura de ordem de serviço) e a seção da Fase 4 (microsserviços, mensageria, saga de pagamento e trace distribuído) |
| [`banco-de-dados.md`](banco-de-dados.md) | Justificativa da escolha do banco, diagrama ER (incluindo a outbox), explicação dos relacionamentos e os bancos dos demais serviços |
| [`rfc/`](rfc/) | RFCs — decisões técnicas relevantes (escolha da nuvem, do banco, da estratégia de autenticação) |
| [`adr/`](adr/) | ADRs — decisões arquiteturais permanentes (Clean Architecture, comunicação, HPA, split em 4 repositórios, State pattern da OS, JWT HS256 compartilhado, token de serviço, mensageria com outbox e saga de pagamento orquestrada) |

Os serviços da Fase 4 têm os próprios ADRs: [`fiap-estoque`](https://github.com/ArthurPeruzzo/fiap-estoque/tree/main/docs/adr) e [`fiap-cobranca`](https://github.com/ArthurPeruzzo/fiap-cobranca/tree/main/docs/adr).
A validação ponta a ponta no ambiente implantado está em `scripts/validacao/` (na raiz do workspace).

Visão geral de runtime, autenticação, observabilidade e deploy: [`../README.md`](../README.md).
