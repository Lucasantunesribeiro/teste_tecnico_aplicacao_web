# Sistema de Gerenciamento de Usuarios — Solution TI

[![CI](https://github.com/Lucasantunesribeiro/teste_tecnico_aplicacao_web/actions/workflows/ci.yml/badge.svg)](https://github.com/Lucasantunesribeiro/teste_tecnico_aplicacao_web/actions/workflows/ci.yml)
[![Security](https://github.com/Lucasantunesribeiro/teste_tecnico_aplicacao_web/actions/workflows/security.yml/badge.svg)](https://github.com/Lucasantunesribeiro/teste_tecnico_aplicacao_web/actions/workflows/security.yml)
[![CodeQL](https://github.com/Lucasantunesribeiro/teste_tecnico_aplicacao_web/actions/workflows/codeql.yml/badge.svg)](https://github.com/Lucasantunesribeiro/teste_tecnico_aplicacao_web/actions/workflows/codeql.yml)

Sistema fullstack para cadastro de usuarios e gerenciamento de enderecos, com autenticacao via **cookies httpOnly**, CSRF protection, cache Redis, consulta de CEP via ViaCEP e pipeline de seguranca completo.

---

## Stack

| Camada | Tecnologias |
|--------|-------------|
| Backend | Java 17, Spring Boot 3.5.13, Spring Security 6, JWT, MapStruct |
| Banco de dados | PostgreSQL 16, Redis 7 |
| Frontend | React 18, TypeScript, Vite, TanStack Query v5, Zustand, React Hook Form + Zod |
| UI | shadcn/ui, Tailwind CSS, Radix UI, Lucide React |
| Testes | JUnit 5, Mockito, Testcontainers, Vitest, Cypress |
| Infra | Docker, Docker Compose, GitHub Actions |
| Seguranca | Gitleaks, CodeQL (SAST), OWASP Dependency Check, Trivy (image scan) |

---

## Funcionalidades

- Autenticacao JWT em **cookies httpOnly** — token nao acessivel ao JavaScript do browser
- Protecao CSRF com double-submit cookie e XOR masking por request
- Cadastro e listagem de usuarios com RBAC (ADMIN / USER)
- Alteracao de senha pelo proprio usuario com confirmacao da senha atual
- CRUD de enderecos com endereco principal unico por usuario
- Promocao automatica de outro endereco ao remover o principal
- Preenchimento automatico por CEP usando ViaCEP com cache Redis
- Dashboard com visoes diferentes para admin e usuario comum
- Listagem global de enderecos paginada para admin
- Rate limiting no login por IP (Bucket4j)
- Correlation ID (MDC) em todos os requests para rastreabilidade
- Swagger/OpenAPI disponivel no ambiente de desenvolvimento

---

## Quick Start com Docker

```bash
cp .env.example .env
# edite .env e defina JWT_SECRET e DB_PASS

docker compose up -d --build
```

| Servico | URL |
|---------|-----|
| Aplicacao | `http://localhost` |
| Swagger | `http://localhost:8080/swagger-ui.html` |
| Health | `http://localhost:8080/actuator/health` |
| Frontend dev (sem Docker) | `http://localhost:5173` |

### Credenciais de teste

| Perfil | CPF | Senha |
|--------|-----|-------|
| Admin | `529.982.247-25` | `Admin123!` |
| Usuario | `390.533.447-05` | `User123!` |

---

## Desenvolvimento local

### Backend

```bash
cp .env.example .env
docker compose up postgres redis -d

cd backend
mvn spring-boot:run
```

API disponivel em `http://localhost:8080`.

### Frontend

```bash
cd frontend
npm ci
npm run dev
```

Frontend disponivel em `http://localhost:5173` (proxy `/api/*` → `:8080`).

---

## API

### Autenticacao (`/api/auth`)

| Metodo | Endpoint | Acesso | Descricao |
|--------|----------|--------|-----------|
| `POST` | `/api/auth/login` | Publico | Login com CPF e senha; define cookies ACCESS\_TOKEN e REFRESH\_TOKEN |
| `POST` | `/api/auth/refresh` | Cookie refresh | Emite novo access token |
| `POST` | `/api/auth/logout` | Autenticado | Invalida sessao e limpa cookies |
| `GET` | `/api/auth/me` | Autenticado | Retorna userId, cpf e role do token atual |

### Usuarios (`/api/usuarios`)

| Metodo | Endpoint | Acesso | Descricao |
|--------|----------|--------|-----------|
| `GET` | `/api/usuarios` | ADMIN | Listar usuarios paginados |
| `POST` | `/api/usuarios` | ADMIN | Criar usuario |
| `GET` | `/api/usuarios/{id}` | Proprio ou ADMIN | Buscar usuario por ID |
| `PUT` | `/api/usuarios/{id}` | ADMIN | Atualizar dados do usuario (senha opcional) |
| `PATCH` | `/api/usuarios/{id}/senha` | Proprio ou ADMIN | Alterar senha — USER exige `senhaAtual`; ADMIN redefine sem confirmacao |
| `DELETE` | `/api/usuarios/{id}` | ADMIN | Deletar usuario |

### Enderecos (`/api/enderecos`)

| Metodo | Endpoint | Acesso | Descricao |
|--------|----------|--------|-----------|
| `GET` | `/api/enderecos` | ADMIN | Listar todos os enderecos paginados |
| `GET` | `/api/enderecos/usuario/{usuarioId}` | Proprio ou ADMIN | Listar enderecos de um usuario |
| `GET` | `/api/enderecos/{id}` | Proprio ou ADMIN | Buscar endereco por ID |
| `POST` | `/api/enderecos` | Autenticado | Criar endereco |
| `PUT` | `/api/enderecos/{id}` | Proprio ou ADMIN | Atualizar endereco |
| `DELETE` | `/api/enderecos/{id}` | Proprio ou ADMIN | Excluir endereco |
| `PATCH` | `/api/enderecos/{id}/principal` | Proprio ou ADMIN | Definir como endereco principal |

### CEP

| Metodo | Endpoint | Acesso | Descricao |
|--------|----------|--------|-----------|
| `GET` | `/api/cep/{cep}` | Autenticado | Consultar CEP via ViaCEP (cache Redis 24h) |

### Nota sobre CSRF

Todas as requisicoes de escrita (`POST`, `PUT`, `PATCH`, `DELETE`) requerem o header:

```
X-XSRF-TOKEN: <valor do cookie XSRF-TOKEN>
```

O cookie `XSRF-TOKEN` e definido automaticamente pelo backend a cada response e deve ser lido
pelo JavaScript antes de cada requisicao de escrita. O Axios ja faz isso automaticamente
via `xsrfCookieName` / `xsrfHeaderName`.

---

## Testes

### Backend (43 metodos)

```bash
cd backend
mvn test          # testes unitarios
mvn verify        # testes unitarios + integração (requer Docker para Testcontainers)
```

| Suite | Metodos | Tipo |
|-------|---------|------|
| AuthServiceTest | 4 | Unitario |
| UsuarioServiceTest | 10 | Unitario |
| ViaCepServiceImplTest | 6 | Unitario |
| AuthControllerIntegrationTest | 7 | Integracao (Testcontainers) |
| EnderecoControllerIntegrationTest | 5 | Integracao (Testcontainers) |
| UsuarioControllerIntegrationTest | 11 | Integracao (Testcontainers) |

### Frontend (43 testes)

```bash
cd frontend
npm ci
npm run lint
npm test -- --run
npm run build
npm audit --omit=dev
```

### E2E

```bash
cd frontend
CYPRESS_BASE_URL=http://localhost npx cypress run
```

---

## Seguranca

### Autenticacao e sessao

- JWT armazenado em **cookies httpOnly** — nao exposto ao JavaScript
- `ACCESS_TOKEN`: cookie httpOnly, path `/`, expira em 15 minutos (padrao)
- `REFRESH_TOKEN`: cookie httpOnly, path `/api/auth`, expira em 7 dias
- `XSRF-TOKEN`: cookie legivel pelo JS (httpOnly=false), renovado a cada response

### Autorizacao — defesa em profundidade

1. Filtro HTTP (`SecurityConfig.authorizeHttpRequests`) — bloqueia antes da resolucao de argumentos
2. `@PreAuthorize("hasRole('ADMIN')")` nos metodos de controller
3. Verificacao no servico (`requireAdmin()` / `isOwner()`)

### Pipeline de seguranca (GitHub Actions)

| Workflow | Gatilho | O que faz |
|----------|---------|-----------|
| `ci.yml` | push / PR | `mvn verify` + `npm ci/lint/test/build` |
| `security.yml` | push main / PR / semanal / manual | Gitleaks por faixa de commits + OWASP Dependency Check + `npm audit` + Trivy (com jobs pesados só quando a área relevante muda no PR) |
| `codeql.yml` | push main / PR / semanal / manual | SAST CodeQL para Java e TypeScript |

---

## Estrutura do projeto

```text
app/
|-- backend/
|   `-- src/main/java/com/solutionti/usuarios/
|       |-- config/         SecurityConfig, RedisConfig, OpenApiConfig
|       |-- controller/     AuthController, UsuarioController, EnderecoController, CepController
|       |-- dto/
|       |   |-- request/    LoginRequest, UsuarioRequest, AtualizarUsuarioRequest,
|       |   |               AlterarSenhaRequest, EnderecoRequest
|       |   `-- response/   LoginResponse, UsuarioResponse, EnderecoResponse, CepResponse, ErrorResponse
|       |-- entity/         Usuario, Endereco
|       |-- exception/      GlobalExceptionHandler, BusinessException, NotFoundException, ForbiddenException
|       |-- mapper/         UsuarioMapper, EnderecoMapper (MapStruct)
|       |-- repository/     UsuarioRepository, EnderecoRepository
|       |-- security/       JwtTokenProvider, JwtAuthenticationFilter, AuthCookieService,
|       |                   CsrfCookieFilter, SpaCsrfTokenRequestHandler, RequestIdFilter
|       |-- service/        Interfaces + Impl para Auth, Usuario, Endereco, Cep
|       `-- validator/      CpfValidator, CepValidator
|-- frontend/
|   `-- src/
|       |-- features/
|       |   |-- auth/       LoginForm, ProtectedRoute, useAuth, authStore (Zustand)
|       |   |-- usuarios/   UsuarioList, UsuarioCard, hooks, service
|       |   `-- enderecos/  EnderecoForm, EnderecoList, CepInput, hooks, service
|       |-- pages/          LoginPage, DashboardPage, UsuariosPage, UsuarioDetailPage,
|       |                   EnderecosPage, AdminEnderecosPage
|       `-- routes/         AppRoutes.tsx (lazy loading em todas as rotas)
|-- .github/
|   `-- workflows/          ci.yml, security.yml, codeql.yml
|-- docs/
|   |-- threat-model.md
|   `-- runbook.md
|-- docker-compose.yml
|-- docker-compose.prod.yml
|-- deploy.sh
`-- .env.example
```

---

## Variaveis de ambiente

| Variavel | Obrigatoria | Padrao | Descricao |
|----------|-------------|--------|-----------|
| `JWT_SECRET` | Sim | — | Chave secreta JWT (base64) |
| `JWT_EXPIRATION` | Nao | `900000` | Expiracao do access token em ms (15 min) |
| `JWT_ACCESS_EXPIRATION` | Nao | `JWT_EXPIRATION` | Substitui `JWT_EXPIRATION` para o access token |
| `JWT_REFRESH_EXPIRATION` | Nao | `604800000` | Expiracao do refresh token em ms (7 dias) |
| `DB_HOST` | Sim | — | Host do PostgreSQL |
| `DB_PORT` | Nao | `5432` | Porta interna do PostgreSQL |
| `DB_NAME` | Nao | `usuarios_db` | Nome do banco |
| `DB_USER` | Nao | `postgres` | Usuario do banco |
| `DB_PASS` | Sim | — | Senha do banco |
| `REDIS_HOST` | Nao | `redis` | Host do Redis |
| `REDIS_PORT` | Nao | `6379` | Porta do Redis |
| `REDIS_PASSWORD` | Sim em prod | — | Senha do Redis |
| `CORS_ORIGINS` | Nao | `http://localhost:5173,http://localhost` | Origens permitidas pelo CORS |
| `TRUST_PROXY_HEADERS` | Nao | `false` | Use `true` apenas atras de proxy confiavel |
| `BACKEND_HOST_PORT` | Nao | `8080` | Porta do host para o backend |
| `FRONTEND_HOST_PORT` | Nao | `80` | Porta do host para o frontend |
| `LOG_LEVEL` | Nao | `INFO` | Nivel de log |
| `JPA_SHOW_SQL` | Nao | `false` | Log de SQL |

> Gere um `JWT_SECRET` seguro com: `openssl rand -base64 64`

---

## Deploy em producao

```bash
cp .env.example .env
# defina JWT_SECRET, DB_PASS e REDIS_PASSWORD com valores seguros
./deploy.sh
```

O script `deploy.sh` usa `docker-compose.prod.yml` que habilita:

- cookies com `Secure` e `SameSite=Strict`
- Redis com autenticacao obrigatoria
- sem Swagger exposto
- health checks com retries

---

Autor: Lucas Ribeiro
