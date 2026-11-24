#!/bin/sh
set -e
# Sin JWT_SECRET se genera una clave aleatoria, unica por instalacion, que se conserva en el volumen.
if [ -z "$JWT_SECRET" ]; then
    if [ ! -s /secretos/jwt ]; then
        (umask 077 && head -c 64 /dev/urandom | base64 | tr -d '\n' > /secretos/jwt)
    fi
    JWT_SECRET=$(cat /secretos/jwt)
    export JWT_SECRET
fi
exec java -Djava.awt.headless=true -Duser.timezone=America/Bogota -XX:MaxRAMPercentage=75 -jar /app/app.jar
