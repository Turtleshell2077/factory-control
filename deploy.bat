@echo off
chcp 65001 >nul
rem Запуск платформы в кластере Docker Swarm на этом компьютере

docker info --format "{{.Swarm.LocalNodeState}}" | findstr /x "active" >nul
if errorlevel 1 (
    echo Создаём кластер Swarm...
    docker swarm init
)

echo Собираем образы...
docker compose build
if errorlevel 1 exit /b 1

echo Разворачиваем стек factory...
docker stack deploy --resolve-image never -c docker-compose.yaml factory

echo.
docker service ls
echo.
echo Панель управления: http://localhost
