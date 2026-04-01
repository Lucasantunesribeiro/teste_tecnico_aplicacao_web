$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root
powershell -ExecutionPolicy Bypass -File ".\scripts\up-local.ps1" -Build
