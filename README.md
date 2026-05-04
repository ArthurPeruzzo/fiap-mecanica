# Mecânica FIAP

API REST para gestão de uma oficina mecânica, desenvolvida como projeto acadêmico na FIAP. O sistema cobre o ciclo completo de uma ordem de serviço — da abertura ao diagnóstico, orçamento, aprovação, execução e entrega — com controle de estoque de peças e insumos e autenticação baseada em perfis de acesso.

## Objetivos

- Gerenciar clientes, veículos, mecânicos e atendentes
- Controlar o ciclo de vida de ordens de serviço
- Gerenciar estoque de peças e insumos com baixa e devolução automática ao vincular/desvincular de ordens
- Restringir operações por perfil: administrador, atendente e mecânico

## Stack

- Java 25
- Spring Boot 4.0.5
- Spring Web MVC
- Spring Security + JWT (Auth0 `java-jwt`)
- Spring Data JPA + Hibernate
- Flyway
- Lombok
- SpringDoc OpenAPI (Swagger UI)
- Testcontainers
- MySQL 8.4

### Justificativa para a escolha do MySQL

A aplicação possui entidades com relacionamentos bem definidos, o que se encaixa naturalmente no modelo relacional. Além disso, o MySQL é amplamente adotado no mercado e possui integração nativa com o ecossistema Spring.

---

> **Aviso:** credenciais de banco de dados e o JWT secret estão expostos neste repositório intencionalmente para facilitar a execução em ambiente de testes e avaliação acadêmica.

## Rodando localmente

### Pré-requisitos

| Via Docker | Via Maven/IDE |
|---|---|
| Docker + Docker Compose | Java 25, Maven, MySQL 8.x |

---

### Opção 1 — Docker (recomendado)

Sobe a aplicação e o banco de dados juntos, sem necessidade de instalar Java ou MySQL localmente.

```bash
# Primeira vez ou após alterações no código
docker compose up --build

# Nas execuções seguintes (sem rebuild)
docker compose up
```

A aplicação ficará disponível em `http://localhost:8080`.

O MySQL ficará acessível no host em `localhost:3307` (útil para conectar com DBeaver ou outro cliente SQL):

| Campo | Valor |
|---|---|
| Host | `127.0.0.1` |
| Porta | `3307` |
| Usuário | `root` |
| Senha | `root` |
| Database | `mecanica` |

Para parar:

```bash
docker compose down        # mantém os dados do banco
docker compose down -v     # apaga os dados do banco também
```

---

### Opção 2 — Maven / IDE

**1. Suba o MySQL localmente** na porta `3306` com database `mecanica` criada.

**2. Configure as variáveis de ambiente** na sua run configuration ou como VM options:

```
DB_URL=jdbc:mysql://127.0.0.1:3306/mecanica
DB_USERNAME=root
DB_PASSWORD=root
JWT_SECRET=<sua-chave-base64url-min-32-bytes>
```

Como VM options (IntelliJ / VS Code):
```
-Dspring.datasource.url=jdbc:mysql://127.0.0.1:3306/mecanica
-Dspring.datasource.username=root
-Dspring.datasource.password=root
-DJWT_SECRET=<sua-chave>
```

**3. Execute a aplicação:**

```bash
./mvnw spring-boot:run
```

As migrations do Flyway rodam automaticamente na inicialização e criam todas as tabelas.

---

## Usuários padrão

Criados automaticamente pelas migrations do Flyway. Todos compartilham a mesma senha:

| E-mail | Senha | Perfil |
|---|---|---|
| `administrador@mecanica.com` | `MeCanica2026!@#` | Administrador |
| `atendente@mecanica.com` | `MeCanica2026!@#` | Atendente |
| `mecanico@mecanica.com` | `MeCanica2026!@#` | Mecânico |

### Autenticando

```http
POST /authenticate/login
Content-Type: application/json

{
  "email": "administrador@mecanica.com",
  "password": "MeCanica2026!@#"
}
```

Use o token retornado no header `Authorization: Bearer <token>` nas demais requisições.

---

## Dados de exemplo

A migration `V14` carrega um conjunto de dados iniciais com clientes, veículos, peças, insumos, serviços e ordens de serviço em todos os status possíveis, prontos para exploração imediata da API.

---

## Documentação da API

Com a aplicação rodando, acesse o Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

---

## Testes
Os testes de integração usam Testcontainers e requerem Docker em execução.

---

## Linguagem Ubíqua

Dicionário de termos do domínio utilizados no sistema.

### Atores e entidades

| Termo | Definição |
|---|---|
| **Cliente** | Pessoa física (CPF) ou jurídica (CNPJ) proprietária de veículos cadastrados no sistema. |
| **Veículo** | Automóvel pertencente a um cliente, identificado pela placa. Um veículo não pode ter mais de uma ordem de serviço ativa simultaneamente. |
| **Atendente** | Funcionário responsável por abrir ordens de serviço, enviar orçamentos ao cliente, registrar a aprovação ou recusa e realizar a entrega do veículo. |
| **Mecânico** | Funcionário responsável por executar o diagnóstico e os serviços vinculados a uma ordem. O mecânico que inicia o diagnóstico torna-se o **mecânico responsável** por aquela ordem. |
| **Administrador** | Perfil com acesso irrestrito ao cadastro de clientes, veículos, peças, insumos e serviços. Não opera ordens de serviço diretamente. |

### Estoque

| Termo | Definição |
|---|---|
| **Peça** | Item físico de estoque. Ex: pastilha de freio, correia dentada. |
| **Insumo** | Item consumível de estoque com unidade de medida variável (litros, ml, unidades). Ex: óleo de motor, fluido de freio. |
| **Baixa de Estoque** | Redução da quantidade disponível de uma peça ou insumo ao vinculá-la a uma ordem em diagnóstico. |
| **Devolução de Estoque** | Restituição da quantidade ao estoque ao desvincular uma peça ou insumo de uma ordem. |
| **Estoque Insuficiente** | Condição que impede o vínculo quando a quantidade solicitada supera o disponível. |

### Ordem de Serviço

| Termo | Definição |
|---|---|
| **Ordem de Serviço (OS)** | Registro central do ciclo de atendimento de um veículo, desde a recepção até a entrega. Agrega serviços, peças e insumos vinculados. |
| **Diagnóstico** | Fase em que o mecânico responsável inspeciona o veículo e determina quais serviços, peças e insumos serão necessários. |
| **Orçamento** | Valor total calculado ao concluir o diagnóstico, somando os preços de todos os serviços, peças e insumos vinculados naquele momento. |
| **Vínculo** | Associação de um serviço, peça ou insumo a uma OS. Permitido apenas durante a fase de diagnóstico. |
| **Mecânico Responsável** | O mecânico que iniciou o diagnóstico de uma OS. Somente ele pode continuar as operações daquela ordem. |

### Ciclo de vida da OS

| Status | Significado |
|---|---|
| **Recebida** | OS criada pelo atendente. Aguardando que um mecânico inicie o diagnóstico. |
| **Em Diagnóstico** | Diagnóstico iniciado pelo mecânico responsável. Permite vincular serviços, peças e insumos. |
| **Diagnóstico Concluído** | Diagnóstico encerrado e orçamento calculado. Aguarda envio ao cliente. |
| **Aguardando Aprovação** | Orçamento enviado ao cliente. Aguarda aprovação ou recusa. |
| **Em Execução** (OS) | Orçamento aprovado. Os serviços vinculados podem ser iniciados e finalizados individualmente pelo mecânico. |
| **Finalizada** | Todos os serviços vinculados foram concluídos. Aguarda entrega do veículo. |
| **Entregue** | Veículo devolvido ao cliente. Estado terminal positivo. |
| **Cancelada** | Orçamento recusado pelo cliente. Estado terminal negativo. Estoque devolvido automaticamente. |

### Execução de serviços

> Os status de serviço são independentes do status da OS. Uma OS pode estar **Em Execução** enquanto cada serviço vinculado evolui individualmente entre os estados abaixo.

| Termo | Definição |
|---|---|
| **Serviço** | Atividade catalogada que pode ser executada pela oficina, com preço e descrição definidos. Ex: alinhamento, troca de óleo. |
| **Serviço Vinculado** | Instância de um serviço associada a uma OS específica, com preço e status de execução próprios. |
| **Não Iniciado** (serviço) | Serviço vinculado à OS mas ainda não iniciado pelo mecânico. |
| **Em Execução** (serviço) | Serviço em andamento pelo mecânico responsável. |
| **Finalizado** (serviço) | Serviço concluído. Quando todos os serviços vinculados atingem este status, a OS transita automaticamente para **Finalizada**. |
| **Tempo Médio de Execução** | Média das durações de todos os serviços finalizados de uma OS. Ausente quando nenhum serviço foi concluído. |
