# Sistema de Gerenciamento de Usuários — Solution TI

Sistema fullstack completo para gerenciamento de usuários e endereços, com autenticação JWT, consulta de CEP via ViaCEP e cache Redis.

## Stack

| Camada | Tecnologias |
|--------|------------|
| **Backend** | Java 17, Spring Boot 3.2, Spring Security 6 (JWT), MapStruct, Flyway |
| **Banco de dados** | PostgreSQL 16 (Supabase), Redis 7 (cache CEP) |
| **Frontend** | React 18, TypeScript, Vite 5, TanStack Query v5, Zustand, React Hook Form + Zod |
| **UI** | shadcn/ui, Tailwind CSS, Radix UI, Lucide React |
| **Testes** | JUnit 5, Mockito, TestContainers, Cypress |
| **Infra** | Docker, Docker Compose |

## Funcionalidades

- Autenticação JWT stateless (login, token com expiração)
- CRUD completo de usuários (paginado, filtros)
- CRUD de endereços por usuário, com consulta automática de CEP (ViaCEP + cache Redis)
- Controle de acesso por papel: `ADMIN` vs `USER`
- Dashboard com estatísticas
- API documentada com Swagger/OpenAPI

## Quick Start com Docker

```bash
# 1. Copie as variáveis de ambiente
cp .env.example .env
# Edite .env e preencha JWT_SECRET (obrigatório)

# 2. Suba tudo
docker compose up -d --build

# Acesse:
# Aplicação:  http://localhost
# Swagger:    http://localhost:8080/swagger-ui.html
# API Health: http://localhost:8080/actuator/health
```

> **Nota:** O script `backend/src/main/resources/db/migration/V1__init.sql` é executado automaticamente no primeiro start do postgres.

## Desenvolvimento local

### Backend

```bash
cd backend

# Copie .env.example e preencha com suas credenciais
cp .env.example .env

# Inicie postgres e redis (Docker)
docker compose up postgres redis -d

# Rode a API
./mvnw spring-boot:run
# API disponível em http://localhost:8080
```

### Frontend

```bash
cd frontend
npm install
npm run dev
# App disponível em http://localhost:5173
```

## Credenciais de Teste

| Perfil | CPF | Senha |
|--------|-----|-------|
| **Admin** | `111.222.333-44` | `admin123` |
| **Usuário** | `555.666.777-88` | `user123` |

## Estrutura do Projeto

```
app/
├── backend/
│   ├── src/main/java/com/solutionti/usuarios/
│   │   ├── controller/        # REST controllers
│   │   ├── service/           # Business logic
│   │   ├── repository/        # Spring Data JPA
│   │   ├── entity/            # JPA entities
│   │   ├── dto/               # Request/Response records
│   │   ├── mapper/            # MapStruct mappers
│   │   ├── security/          # JWT, filters, config
│   │   ├── validator/         # CPF/CEP validators
│   │   └── exception/         # Global error handling
│   └── src/main/resources/db/migration/  # Flyway migrations
├── frontend/
│   └── src/
│       ├── features/          # Auth, Usuarios, Enderecos
│       ├── components/        # shadcn/ui + layout
│       ├── lib/               # Axios, QueryClient, utils
│       ├── pages/             # Route pages
│       └── routes/            # React Router config
├── docker-compose.yml         # Desenvolvimento
├── docker-compose.prod.yml    # Produção
├── deploy.sh                  # Script de deploy
└── .env.example               # Template de variáveis
```

## API Endpoints

| Método | Endpoint | Acesso | Descrição |
|--------|----------|--------|-----------|
| `POST` | `/api/auth/login` | Público | Login (CPF + senha) |
| `GET` | `/api/usuarios` | ADMIN | Listar usuários (paginado) |
| `POST` | `/api/usuarios` | Público | Criar usuário |
| `GET` | `/api/usuarios/{id}` | Próprio/ADMIN | Buscar usuário |
| `PUT` | `/api/usuarios/{id}` | Próprio/ADMIN | Atualizar usuário |
| `DELETE` | `/api/usuarios/{id}` | ADMIN | Deletar usuário |
| `GET` | `/api/enderecos` | Autenticado | Listar endereços do usuário |
| `POST` | `/api/enderecos` | Autenticado | Criar endereço |
| `DELETE` | `/api/enderecos/{id}` | Próprio/ADMIN | Deletar endereço |
| `PUT` | `/api/enderecos/{id}/principal` | Próprio/ADMIN | Marcar como principal |
| `GET` | `/api/cep/{cep}` | Autenticado | Consultar CEP (ViaCEP + Redis cache) |

## Testes

```bash
# Testes unitários (sem Docker necessário)
cd backend && ./mvnw test -Dtest="CpfValidatorTest,CepValidatorTest,AuthServiceTest,UsuarioServiceTest"

# Testes de integração (requer Docker para TestContainers)
cd backend && ./mvnw verify

# Testes E2E (requer backend + frontend rodando)
cd frontend
npm install
npm run test:e2e       # headless
npm run cypress:open   # interativo
```

## Deploy em Produção

```bash
cp .env.example .env
# Preencha todas as variáveis (especialmente JWT_SECRET e DB_PASS)
./deploy.sh
```

## Variáveis de Ambiente

| Variável | Obrigatória | Padrão | Descrição |
|----------|-------------|--------|-----------|
| `JWT_SECRET` | **Sim** | — | Chave secreta JWT (base64, mín. 256 bits) |
| `DB_HOST` | Sim | — | Host PostgreSQL |
| `DB_NAME` | Não | `usuarios_db` | Nome do banco |
| `DB_USER` | Não | `postgres` | Usuário do banco |
| `DB_PASS` | **Sim** | — | Senha do banco |
| `REDIS_HOST` | Não | `localhost` | Host Redis |
| `JWT_EXPIRATION` | Não | `86400000` | Expiração do token (ms) |

> Gere um JWT_SECRET seguro: `openssl rand -base64 64`

---

**Autor:** Lucas Ribeiro | Teste Técnico Solution TI
