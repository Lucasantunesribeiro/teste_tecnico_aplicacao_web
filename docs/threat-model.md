# Threat Model — Sistema de Gerenciamento de Usuários

**Versão:** 2.0  
**Data:** 2026-04-02  
**Metodologia:** STRIDE  
**Escopo:** API REST (Spring Boot) + SPA (React) + PostgreSQL + Redis

---

## 1. Diagrama de Limites de Confiança

```
┌─────────────────────────────────────────────────────────────────┐
│  INTERNET (não confiável)                                       │
│                                                                  │
│   Browser ──HTTPS──► Nginx (SPA + reverse proxy)               │
│                              │                                  │
└──────────────────────────────┼──────────────────────────────────┘
                               │ rede Docker privada (app-network)
           ┌───────────────────┼───────────────────┐
           │  BACKEND TRUST BOUNDARY                │
           │         ▼                              │
           │  Spring Boot API  ──► PostgreSQL 16    │
           │         │         ──► Redis 7          │
           │         │         ──► ViaCEP (HTTPS)   │
           │         │                              │
           └─────────┼──────────────────────────────┘
                     │ acesso externo bloqueado por padrão
                     │ (sem exposição de portas de BD/Redis)
```

**Limites:**
- Tráfego externo chega apenas na porta 80 (Nginx).
- PostgreSQL e Redis não têm portas expostas no host em produção.
- Comunicação interna via rede Docker bridge (`app-network`).

---

## 2. Ativos e Classificação

| Ativo | Confidencialidade | Integridade | Disponibilidade |
|-------|-------------------|-------------|-----------------|
| Dados pessoais (CPF, nome, data de nascimento) | Alta | Alta | Média |
| Endereços residenciais | Alta | Alta | Média |
| Hash de senhas (bcrypt) | Alta | Alta | Baixa |
| ACCESS_TOKEN JWT (cookie httpOnly) | Alta | Alta | Alta |
| REFRESH_TOKEN (cookie httpOnly, Redis) | Alta | Alta | Alta |
| JWT_SECRET (env var) | Crítica | Crítica | Baixa |
| Credenciais de banco/Redis (env vars) | Crítica | Crítica | Baixa |
| XSRF-TOKEN (cookie legível por JS) | Baixa | Alta | Alta |

---

## 3. Atores e Nível de Confiança

| Ator | Confiança | Descrição |
|------|-----------|-----------|
| Administrador autenticado | Alta | Sessão válida com `ROLE_ADMIN` |
| Usuário autenticado | Média | Sessão válida com `ROLE_USER` — acesso apenas a dados próprios |
| Atacante externo não autenticado | Zero | Sem credenciais válidas |
| Atacante com XSS no navegador | Muito baixa | JavaScript malicioso em contexto de sessão ativa |
| Atacante interno (rede Docker) | Baixa | Acesso ao `app-network` — sem credenciais de BD |
| Pipeline CI (GitHub Actions) | Alta | Lê código-fonte e variáveis de ambiente do runner |

---

## 4. Tabela de Ameaças STRIDE

### 4.1 Spoofing (Falsificação de Identidade)

| ID | Ameaça | Componente | Mitigação | Residual |
|----|--------|-----------|-----------|---------|
| S1 | Roubo de sessão via XSS para uso de cookies | Browser | Cookies `ACCESS_TOKEN` e `REFRESH_TOKEN` são `httpOnly` — JS não pode lê-los | XSS ainda pode fazer requisições autenticadas sem ler o token |
| S2 | Brute-force de credenciais | `POST /api/auth/login` | Rate limiting por IP com Bucket4j (falha → 429) | Atacante com múltiplos IPs (botnet) |
| S3 | Reuso de refresh token expirado/revogado | `POST /api/auth/refresh` | Tokens armazenados no Redis com TTL; `rotate()` revoga o token anterior | — |
| S4 | CPF/senha genéricos inferidos por enumeração | `POST /api/auth/login` | Resposta uniforme `401 "Credenciais inválidas"` para CPF inexistente e senha errada | Timing side-channel teórico (mitigado pelo bcrypt que equaliza latência) |

### 4.2 Tampering (Adulteração de Dados)

| ID | Ameaça | Componente | Mitigação | Residual |
|----|--------|-----------|-----------|---------|
| T1 | CSRF — requisição forjada por site malicioso | Endpoints de escrita | Double-submit CSRF com `X-XSRF-TOKEN`; XOR masking por request via `SpaCsrfTokenRequestHandler` | Requer comprometimento adicional para vazar o cookie XSRF |
| T2 | Adulteração de payload JWT | `JwtAuthenticationFilter` | Assinatura HMAC-SHA256 com `JWT_SECRET`; qualquer payload adulterado invalida a assinatura | Segredo fraco ou vazado |
| T3 | Escalonamento de privilégios (USER → ADMIN) | `UsuarioController`, `EnderecoController` | Defesa em profundidade: (1) HTTP-level `hasRole("ADMIN")`, (2) `@PreAuthorize`, (3) verificação no serviço | — |
| T4 | Acesso horizontal entre usuários | `GET /api/usuarios/{id}`, `GET /api/enderecos/{id}` | `isOwner()` no serviço compara `principal.getId()` com o ID do recurso | — |
| T5 | Injeção SQL via parâmetros | Camada de repositório | Spring Data JPA com parâmetros nomeados (sem concatenação de SQL) | — |

### 4.3 Repudiation (Repúdio)

| ID | Ameaça | Componente | Mitigação | Residual |
|----|--------|-----------|-----------|---------|
| R1 | Usuário nega ter realizado ação | Todos os endpoints | Correlation ID (MDC `requestId`) em todos os logs; logs estruturados com IP, userId e ação | Logs em container local — sem persistência fora do ciclo de vida do container em dev |
| R2 | Alteração de senha sem evidência | `PATCH /api/usuarios/{id}/senha` | Log `INFO` com userId antes da alteração | Ausente em produção se nível de log for WARN (intencional — senha não é logada) |

### 4.4 Information Disclosure (Divulgação de Informação)

| ID | Ameaça | Componente | Mitigação | Residual |
|----|--------|-----------|-----------|---------|
| I1 | Stack trace exposto na resposta de erro | `GlobalExceptionHandler` | Respostas padronizadas `ErrorResponse` sem stack trace; apenas mensagem de negócio | — |
| I2 | SQL queries expostas nos logs | `application.yml` | `JPA_SHOW_SQL=false` em produção | — |
| I3 | Segredos nos logs | Filtros e serviços | Senhas nunca logadas; tokens logados apenas por ID de correlação | — |
| I4 | Swagger exposto em produção | `OpenApiConfig` | Springdoc desabilitado quando `spring.profiles.active=prod` | — |
| I5 | Variáveis de ambiente sensíveis expostas | `docker-compose.prod.yml` | Env vars injetadas em runtime via `.env`; `.env` no `.gitignore` | Se o host for comprometido, env vars são legíveis pelo processo |
| I6 | CEP exposto em logs de cache Redis | `CepService` | Chave Redis é `cep:{numero}` — sem PII diretamente logado | — |

### 4.5 Denial of Service (Negação de Serviço)

| ID | Ameaça | Componente | Mitigação | Residual |
|----|--------|-----------|-----------|---------|
| D1 | Flood de tentativas de login | `POST /api/auth/login` | Rate limiting por IP; Bucket4j retorna `429 Too Many Requests` | DDoS distribuído com IPs rotativos |
| D2 | Consultas de CEP excessivas para ViaCEP externo | `GET /api/cep/{cep}` | Cache Redis com TTL de 24h; hit de cache evita chamada externa | Atacante com muitos CEPs distintos — sem paginação de resultados da ViaCEP |
| D3 | Payloads gigantes na criação de usuário | `POST /api/usuarios` | Spring Boot `max-request-size` padrão (1MB); Bean Validation limita tamanho de campos | — |
| D4 | Exaustão de conexões PostgreSQL | Pool de conexões | HikariCP com pool gerenciado pelo Spring Boot | — |

### 4.6 Elevation of Privilege (Escalonamento de Privilégio)

| ID | Ameaça | Componente | Mitigação | Residual |
|----|--------|-----------|-----------|---------|
| E1 | USER acessa endpoint ADMIN | `UsuarioController`, `EnderecoController` | SecurityConfig → `hasRole("ADMIN")` no HTTP-level (executa antes de `@Valid`) + `@PreAuthorize` + `requireAdmin()` no serviço | — |
| E2 | Promoção não autorizada de USER para ADMIN | `PUT /api/usuarios/{id}` | ADMIN-only; campo `tipo` não é atualizável via `AtualizarUsuarioRequest` | — |
| E3 | Container escapa para o host | Docker runtime | Containers executam sem `--privileged`; usuário não-root no Dockerfile | Vulnerabilidade 0-day no kernel ou runtime Docker |

---

## 5. Registro de Riscos Residuais

| ID | Risco | Probabilidade | Impacto | Aceitação | Justificativa |
|----|-------|---------------|---------|-----------|---------------|
| RR1 | XSS abusa da sessão sem ler cookies | Baixa | Alto | Aceito | CSP não configurado; depende de código React sem `dangerouslySetInnerHTML` e de dependências auditadas via `npm audit` |
| RR2 | DDoS distribuído ultrapassa rate limit por IP | Média | Médio | Aceito | Requer WAF/CDN fora do escopo da aplicação |
| RR3 | JWT_SECRET comprometido — todos os tokens invalidados | Muito baixa | Crítico | Aceito (com rotação) | Mitigado por `openssl rand -base64 64` na criação e rotação periódica; tokens têm TTL curto (15 min) |
| RR4 | CVEs em dependências sem atualização | Média | Variável | Monitorado | OWASP Dependency Check + Trivy image scan + `npm audit` via CI; em PR os jobs pesados rodam quando a área relevante muda |
| RR5 | Logs sem persistência entre reinicializações em dev | Alta | Baixo | Aceito (dev only) | Produção deve montar volume ou usar driver de log externo (CloudWatch, Loki) |

---

## 6. Controles de Segurança — Resumo

| Camada | Controle |
|--------|---------|
| Transporte | HTTPS (nginx em produção com `Secure` nos cookies) |
| Autenticação | JWT httpOnly cookies; bcrypt (fator 10) para senhas |
| Sessão | ACCESS_TOKEN 15 min; REFRESH_TOKEN 7 dias com rotação e revogação no Redis |
| Autorização | SecurityConfig HTTP-level → `@PreAuthorize` AOP → `requireAdmin()`/`isOwner()` |
| CSRF | Double-submit cookie com XOR masking por request |
| Brute-force | Rate limiting por IP (Bucket4j) em `/api/auth/login` |
| Enumeração | Resposta de erro uniforme em autenticação |
| Logging | Correlation ID (MDC) em todos os requests; JSON estruturado em produção |
| CI/CD | Gitleaks (secret scan) + CodeQL (SAST) + OWASP Dependency Check (SCA) + Trivy (image scan) |

---

## 7. Premissas e Fora de Escopo

**Premissas:**
- O host Docker é seguro e monitorado.
- As variáveis de ambiente de produção (JWT_SECRET, DB_PASS, REDIS_PASSWORD) são gerenciadas por um cofre de segredos (Vault, AWS Secrets Manager) ou equivalente.
- O proxy reverso (Nginx) termina TLS e define os headers `Secure` e `SameSite=Strict` nos cookies.

**Fora de escopo:**
- Ataques à infraestrutura de rede (DDoS volumétrico, BGP hijacking).
- Comprometimento físico do servidor.
- Engenharia social contra administradores.
