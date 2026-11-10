@echo off
chcp 65001 >nul
rem Остановка платформы (данные базы остаются в томе factory_db-data)
docker stack rm factory
