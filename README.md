# Sistema de Gerenciamento de Usuarios - Solution TI

Sistema fullstack para cadastro de usuarios e gerenciamento de enderecos, com autenticacao JWT, consulta de CEP via ViaCEP e cache Redis no backend.

## Stack

| Camada | Tecnologias |
|--------|-------------|
| Backend | Java 17, Spring Boot 3.2, Spring Security 6, JWT, MapStruct |
| Banco de dados | PostgreSQL 16, Redis 7 |
| Frontend | React 18, TypeScript, Vite 5, TanStack Query v5, Zustand, React Hook Form + Zod |
| UI | shadcn/ui, Tailwind CSS, Radix UI, Lucide React |
| Testes | JUnit 5, Mockito, Testcontainers, Vitest, Cypress |
| Infra | Docker, Docker Compose |

## Funcionalidades

- Autenticacao JWT stateless
- Cadastro e listagem de usuarios com RBAC
- CRUD de enderecos com endereco principal unico por usuario
- Preenchimento automatico por CEP usando ViaCEP
- Cache de CEP no backend para evitar consultas repetidas
- Dashboard com visoes diferentes para admin e usuario comum
- Swagger/OpenAPI no ambiente de desenvolvimento

## Quick Start com Docker

```bash
cp .env.example .env
# preencha JWT_SECRET e DB_PASS

docker compose up -d --build
```

Acessos locais:

- Aplicacao: `http://localhost`
- Frontend dev: `http://localhost:5173`
- Swagger: `http://localhost:8080/swagger-ui.html`
- Healthcheck: `http://localhost:8080/actuator/health`

## Desenvolvimento local

### Backend

No diretorio `app/`:

```bash
cp .env.example .env
docker compose up postgres redis -d
cd backend
mvn spring-boot:run
```

API disponivel em `http://localhost:8080`.

### Frontend

No diretorio `app/`:

```bash
cd frontend
npm ci
npm run dev
```

Frontend disponivel em `http://localhost:5173`.

## Credenciais de teste

| Perfil | CPF | Senha |
|--------|-----|-------|
| Admin | `529.982.247-25` | `Admin123!` |
| Usuario | `390.533.447-05` | `User123!` |

## Estrutura

```text
app/
|-- backend/
|   |-- src/main/java/com/solutionti/usuarios/
|   |   |-- controller/
|   |   |-- service/
|   |   |-- repository/
|   |   |-- entity/
|   |   |-- dto/
|   |   |-- mapper/
|   |   |-- security/
|   |   |-- validator/
|   |   `-- exception/
|   `-- src/main/resources/db/migration/
|-- frontend/
|   `-- src/
|       |-- components/
|       |-- features/
|       |-- lib/
|       |-- pages/
|       `-- routes/
|-- docker-compose.yml
|-- docker-compose.prod.yml
|-- deploy.sh
`-- .env.example
```

## API

| Metodo | Endpoint | Acesso | Descricao |
|--------|----------|--------|-----------|
| `POST` | `/api/auth/login` | Publico | Login com CPF e senha |
| `GET` | `/api/usuarios` | ADMIN | Listar usuarios |
| `POST` | `/api/usuarios` | ADMIN | Criar usuario |
| `GET` | `/api/usuarios/{id}` | Proprio ou ADMIN | Buscar usuario |
| `PUT` | `/api/usuarios/{id}` | ADMIN | Atualizar usuario |
| `DELETE` | `/api/usuarios/{id}` | ADMIN | Deletar usuario |
| `GET` | `/api/enderecos/usuario/{usuarioId}` | Proprio ou ADMIN | Listar enderecos do usuario |
| `POST` | `/api/enderecos` | Autenticado | Criar endereco |
| `PUT` | `/api/enderecos/{id}` | Proprio ou ADMIN | Atualizar endereco |
| `DELETE` | `/api/enderecos/{id}` | Proprio ou ADMIN | Excluir endereco |
| `PATCH` | `/api/enderecos/{id}/principal` | Proprio ou ADMIN | Definir endereco principal |
| `GET` | `/api/cep/{cep}` | Autenticado | Consultar CEP |

## Testes

Backend:

```bash
cd backend
mvn test
mvn verify
```

Frontend:

```bash
cd frontend
npm ci
npm run lint
npm test -- --run
npm run build
```

E2E:

```bash
cd frontend
npm run test:e2e
```

## Deploy em producao

```bash
cp .env.example .env
# preencha JWT_SECRET, DB_PASS e REDIS_PASSWORD
./deploy.sh
```

## Variaveis de ambiente

| Variavel | Obrigatoria | Padrao | Descricao |
|----------|-------------|--------|-----------|
| `JWT_SECRET` | Sim | - | Chave secreta JWT |
| `JWT_EXPIRATION` | Nao | `86400000` | Expiracao do token em ms |
| `DB_HOST` | Sim | - | Host do PostgreSQL |
| `DB_PORT` | Nao | `5432` | Porta do PostgreSQL |
| `DB_NAME` | Nao | `usuarios_db` | Nome do banco |
| `DB_USER` | Nao | `postgres` | Usuario do banco |
| `DB_PASS` | Sim | - | Senha do banco |
| `REDIS_HOST` | Nao | `localhost` | Host do Redis |
| `REDIS_PORT` | Nao | `6379` | Porta do Redis |
| `REDIS_PASSWORD` | Sim em prod | - | Senha do Redis |
| `TRUST_PROXY_HEADERS` | Nao | `false` | Use `true` apenas atras de proxy confiavel |
| `LOG_LEVEL` | Nao | `INFO` | Nivel de log da aplicacao |
| `JPA_SHOW_SQL` | Nao | `false` | Log de SQL no backend |

> Gere um `JWT_SECRET` seguro com `openssl rand -base64 64`.

---

Autor: Lucas Ribeiro
