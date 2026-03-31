# Solution TI - Sistema de Gerenciamento de Usuários

Sistema fullstack para gerenciamento de usuários e endereços com autenticação JWT.

## Stack

- **Backend:** Java 17 + Spring Boot 3.2, PostgreSQL, Redis, JWT
- **Frontend:** React 18 + TypeScript, Vite, Tailwind CSS, shadcn/ui
- **Infra:** Docker + Docker Compose

## Quick Start

```bash
docker-compose up -d
# Frontend: http://localhost:5173
# API:      http://localhost:8080
# Swagger:  http://localhost:8080/swagger-ui.html
```

## Credenciais de Teste

| Tipo  | CPF         | Senha    |
|-------|-------------|----------|
| Admin | 11122233344 | admin123 |
| User  | 55566677788 | user123  |

## Estrutura

```
app/
├── backend/    # API Spring Boot
├── frontend/   # Aplicação React
├── docker/     # Configs Docker
└── docs/       # Documentação
```
