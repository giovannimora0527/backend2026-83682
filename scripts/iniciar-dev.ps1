# Arranca el backend en segundo plano con la base del .env y el correo en Mailpit (http://localhost:8025).
# Uso:  powershell -ExecutionPolicy Bypass -File scripts\iniciar-dev.ps1
# Parar: powershell -ExecutionPolicy Bypass -File scripts\iniciar-dev.ps1 -Parar
param([switch]$Parar)
$ErrorActionPreference = 'Stop'
$raiz = Split-Path $PSScriptRoot -Parent
$dir = Join-Path $env:LOCALAPPDATA 'clinica-dev'
$jar = Join-Path $dir 'clinica.jar'
New-Item -ItemType Directory -Force $dir | Out-Null

# Solo se detiene el proceso que ejecuta esta copia del JAR.
Get-CimInstance Win32_Process -Filter "Name = 'java.exe'" |
    Where-Object { $_.CommandLine -and $_.CommandLine.Contains($jar) } |
    ForEach-Object { Stop-Process -Id $_.ProcessId -Force; Wait-Process -Id $_.ProcessId -ErrorAction SilentlyContinue }
if ($Parar) { Write-Output 'Backend detenido.'; exit 0 }

if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) {
    Write-Output 'El puerto 8080 esta ocupado por otro programa.'; exit 1
}
if (docker ps -aq --filter 'name=^clinica-mailpit$') { docker start clinica-mailpit | Out-Null }
else { docker run -d --name clinica-mailpit -p 127.0.0.1:8025:8025 -p 127.0.0.1:1025:1025 axllent/mailpit | Out-Null }
if ($LASTEXITCODE -ne 0) { Write-Output 'No se pudo iniciar Mailpit. Abra Docker Desktop.'; exit 1 }

Push-Location $raiz
try {
    .\mvnw.cmd -q -DskipTests package
    if ($LASTEXITCODE -ne 0) { Write-Output 'La compilacion fallo.'; exit 1 }
    Copy-Item (Get-ChildItem target\clinica-*.jar | Select-Object -First 1).FullName $jar -Force
    $opciones = @('-jar', ('"' + $jar + '"'), '--spring.mail.host=localhost', '--spring.mail.port=1025',
        '--spring.mail.properties.mail.smtp.auth=false', '--spring.mail.properties.mail.smtp.starttls.enable=false',
        '--spring.jpa.show-sql=false')
    Start-Process java -ArgumentList $opciones -WorkingDirectory $raiz -WindowStyle Hidden `
        -RedirectStandardOutput (Join-Path $dir 'backend.log') -RedirectStandardError (Join-Path $dir 'backend-error.log')
} finally { Pop-Location }

for ($i = 0; $i -lt 60; $i++) {
    Start-Sleep -Seconds 2
    try {
        Invoke-WebRequest -UseBasicParsing 'http://localhost:8080/clinica/v1/clinica/test' -TimeoutSec 3 | Out-Null
        Write-Output "Backend listo en http://localhost:8080/clinica/v1 (registro: $dir\backend.log)"; exit 0
    } catch { }
}
Write-Output "El backend no respondio. Revise $dir\backend.log"; exit 1
