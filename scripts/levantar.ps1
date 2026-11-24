# Levanta todo con Docker Compose, sin que Windows se suspenda mientras tanto, y muestra cuanto tardo.
# Uso: powershell -ExecutionPolicy Bypass -File scripts\levantar.ps1 [-DesdeCero]
# -DesdeCero borra la base de datos de los contenedores (no toca el MySQL instalado en el equipo).
param([switch]$DesdeCero)
$raiz = Split-Path $PSScriptRoot -Parent
Add-Type -Namespace Clinica -Name Energia -MemberDefinition '[DllImport("kernel32.dll")] public static extern uint SetThreadExecutionState(uint f);'
# ES_CONTINUOUS | ES_SYSTEM_REQUIRED: solo mientras dura este script. No evita la suspension al cerrar la tapa.
[Clinica.Energia]::SetThreadExecutionState([uint32]2147483649) | Out-Null
Push-Location $raiz
try {
    $reloj = [Diagnostics.Stopwatch]::StartNew()
    if ($DesdeCero) { docker compose down -v }
    docker compose up --build -d --wait
    if ($LASTEXITCODE -ne 0) { docker compose ps; Write-Output 'No se pudo levantar todo. Revise: docker compose logs'; exit 1 }
    $reloj.Stop()
    docker compose ps --format 'table {{.Service}}\t{{.Status}}'
    Write-Output ('Listo en {0:N0} segundos.' -f $reloj.Elapsed.TotalSeconds)
} finally {
    [Clinica.Energia]::SetThreadExecutionState([uint32]2147483648) | Out-Null
    Pop-Location
}
