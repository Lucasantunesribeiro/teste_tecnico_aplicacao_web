param(
  [switch]$Build
)

$ErrorActionPreference = "Stop"

function Use-DefaultIfMissingOrPlaceholder {
  param(
    [string]$Name,
    [string]$DefaultValue
  )

  $current = [Environment]::GetEnvironmentVariable($Name)
  if ([string]::IsNullOrWhiteSpace($current) -or $current.StartsWith("<YOUR_")) {
    [Environment]::SetEnvironmentVariable($Name, $DefaultValue)
    Write-Host "Usando valor local padrao para $Name."
  }
}

function Test-PortInUse {
  param([int]$Port)

  $listener = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue
  return $null -ne $listener
}

function Resolve-HostPort {
  param(
    [string]$Name,
    [int[]]$Candidates
  )

  foreach ($candidate in $Candidates) {
    if (-not (Test-PortInUse -Port $candidate)) {
      if ($candidate -ne $Candidates[0]) {
        Write-Host "Porta $($Candidates[0]) ocupada para $Name. Usando fallback $candidate."
      }
      return $candidate
    }
  }

  throw "Nenhuma porta livre encontrada para $Name. Candidatas testadas: $($Candidates -join ', ')"
}

$env:DB_HOST_PORT = [string](Resolve-HostPort -Name "PostgreSQL" -Candidates @(5432, 5433, 15432, 25432, 35432))
$env:REDIS_HOST_PORT = [string](Resolve-HostPort -Name "Redis" -Candidates @(6379, 6380, 16379, 26379))
$env:BACKEND_HOST_PORT = [string](Resolve-HostPort -Name "Backend" -Candidates @(8080, 8081, 18080, 28080))
$env:FRONTEND_HOST_PORT = [string](Resolve-HostPort -Name "Frontend" -Candidates @(80, 8088, 3000, 4173))

Use-DefaultIfMissingOrPlaceholder -Name "DB_NAME" -DefaultValue "usuarios_db"
Use-DefaultIfMissingOrPlaceholder -Name "DB_USER" -DefaultValue "postgres"
Use-DefaultIfMissingOrPlaceholder -Name "DB_PASS" -DefaultValue "postgres"
Use-DefaultIfMissingOrPlaceholder -Name "JWT_SECRET" -DefaultValue "ZGV2LWp3dC1zZWNyZXQtZm9yLWxvY2FsLXRlc3Rpbmctb25seS1yZXBsYWNlLWluLXByb2Q="

$composeArgs = @("compose", "up", "-d")
if ($Build) {
  $composeArgs += "--build"
}

Write-Host "Subindo stack local com portas:"
Write-Host "  PostgreSQL -> $($env:DB_HOST_PORT)"
Write-Host "  Redis      -> $($env:REDIS_HOST_PORT)"
Write-Host "  Backend    -> $($env:BACKEND_HOST_PORT)"
Write-Host "  Frontend   -> $($env:FRONTEND_HOST_PORT)"

docker @composeArgs
if ($LASTEXITCODE -ne 0) {
  throw "docker compose falhou com codigo $LASTEXITCODE"
}

Write-Host ""
Write-Host "Stack iniciada."
Write-Host "Frontend: http://localhost:$($env:FRONTEND_HOST_PORT)"
Write-Host "Backend:  http://localhost:$($env:BACKEND_HOST_PORT)"
