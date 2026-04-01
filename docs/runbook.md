# Runbook

## Checklist rapido

- validar `docker compose ps`
- validar backend em `/actuator/health`
- validar frontend em `http://localhost`
- conferir `REDIS_HOST_PORT` efetivo no ambiente local

## Subida local

### Windows

```powershell
./start-local.ps1
```

### Linux e macOS

```bash
./start-local.sh
```

Os scripts tentam usar `6379` para o bind do Redis. Se a porta estiver ocupada, fazem fallback automatico para `6380`.

## Incidentes comuns

### `Bind for 0.0.0.0:6379 failed`

- causa: outra instancia de Redis ja esta usando a porta
- mitigacao suportada: usar `start-local.ps1` ou `start-local.sh`
- alternativa manual: definir `REDIS_HOST_PORT=6380` antes do `docker compose up`

### `401` apos login

- validar se os cookies de sessao foram emitidos
- confirmar `JWT_SECRET` consistente
- verificar se frontend e backend usam o mesmo host de navegacao

### `429` no login

- verificar rate limit no Redis
- limpar chaves `login:attempts:*` apenas em desenvolvimento

## Rollback

- parar a stack atual
- restaurar imagem ou tag anterior
- subir novamente com o mesmo `.env`
- revalidar `/actuator/health` e fluxo de login
