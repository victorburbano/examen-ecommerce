# Recrea los symlinks relativos .claude/skills/<skill> -> ../../.agents/skills/<skill>
# Requiere Modo desarrollador de Windows o una consola con privilegios de administrador.
# Recomendado: git config core.symlinks true
# Se usa mklink porque el destino relativo se resuelve desde la ubicacion del enlace.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$linksDir = Join-Path $root '.claude\skills'
New-Item -ItemType Directory -Force -Path $linksDir | Out-Null
Get-ChildItem -Directory (Join-Path $root '.agents\skills') | ForEach-Object {
    $link = Join-Path $linksDir $_.Name
    if (Test-Path $link) { cmd /c rmdir "$link" }
    cmd /c mklink /D "$link" "..\..\.agents\skills\$($_.Name)" | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "mklink failed for $($_.Name) (activa el Modo desarrollador o ejecuta como administrador)" }
    Write-Host "linked $($_.Name)"
}
