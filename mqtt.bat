@echo off
chcp 65001 >nul
rem Показывает все сообщения MQTT, которые проходят через брокер. Выход: Ctrl+C.
rem   mqtt.bat                       - все топики factory/...
rem   mqtt.bat factory/2/#           - только станок №2

set BR=
for /f %%i in ('docker ps -q -f name^=factory_broker') do set BR=%%i
if "%BR%"=="" (
    echo Контейнер брокера не найден. Сначала запустите deploy.bat
    exit /b 1
)

set TOPIC=%~1
if "%TOPIC%"=="" set TOPIC=factory/#
docker exec %BR% mosquitto_sub -t "%TOPIC%" -v
