# Mecânica FIAP

API REST para gestão de uma oficina mecânica, desenvolvida como projeto acadêmico na FIAP. O sistema cobre o ciclo completo de uma ordem de serviço — da abertura ao diagnóstico, orçamento, aprovação, execução e entrega — com controle de estoque de peças e insumos e autenticação baseada em perfis de acesso.

## Objetivos

- Gerenciar clientes, veículos, mecânicos e atendentes
- Controlar o ciclo de vida de ordens de serviço
- Gerenciar estoque de peças e insumos com baixa e devolução automática ao vincular/desvincular de ordens
- Restringir operações por perfil: administrador, atendente e mecânico

## Stack

- Java 25 + Spring Boot 4.0.5
- Spring Web MVC, Spring Security, Spring Data JPA
- MySQL 8.4 + Flyway
- Autenticação JWT (Auth0 `java-jwt`)
- Documentação via SpringDoc OpenAPI (Swagger UI)

---

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
