@echo off
chcp 65001 >nul
rem Запуск платформы в кластере Docker Swarm на этом компьютере

docker info --format "{{.Swarm.LocalNodeState}}" | findstr /b /c:"active" >nul
if errorlevel 1 (
    echo Создаём кластер Swarm...
    docker swarm init
)

rem если стек уже запущен, после сборки его сервисы нужно перезапустить
set RESTART=
docker stack ls | findstr /b /c:"factory " >nul
if not errorlevel 1 set RESTART=1

echo Собираем образы...
docker compose build
if errorlevel 1 exit /b 1

echo Разворачиваем стек factory...
docker stack deploy --resolve-image never -c docker-compose.yaml factory
if errorlevel 1 exit /b 1

rem образы называются так же (:latest), поэтому Swarm сам не видит, что они пересобраны
if defined RESTART (
    echo Перезапускаем сервисы с новыми образами...
    for %%s in (proxy frontend backend emulator db broker) do docker service update --force --detach factory_%%s >nul
)

echo.
docker service ls
echo.
echo Панель управления: http://localhost
