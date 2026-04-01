# Threat Model

## Ativos

- dados pessoais de usuarios
- enderecos
- access tokens
- refresh tokens
- credenciais de banco e Redis

## Atores

- usuario autenticado legitimo
- administrador legitimo
- atacante externo sem autenticacao
- atacante com XSS no browser
- atacante tentando brute force ou enumeracao

## Vetores principais

- roubo ou abuso de sessao no browser
- brute force no login
- enumeracao de contas
- acesso horizontal entre usuarios
- abuso de refresh token
- exposicao indevida por CORS ou cookies
- vazamento de segredos no repositorio ou pipeline

## Controles

- cookies `httpOnly` para sessao
- refresh rotation com revogacao server-side
- CSRF token em mutacoes
- RBAC no backend
- validacao uniforme de credenciais invalidas
- rate limit de login
- CORS explicito
- request id e audit trail
- CI com secret scan, SAST, SCA e image scan

## Riscos residuais

- XSS ainda pode abusar da sessao do navegador, mesmo sem ler cookies
- dependencias de desenvolvimento podem voltar a introduzir CVEs sem gates de atualizacao
- configuracoes inseguras de proxy podem comprometer rate limit se forem reabertas
