@echo off
chcp 65001 >nul
rem Консоль PostgreSQL внутри контейнера базы данных.
rem   db.bat                                     - интерактивная консоль (выход: \q)
rem   db.bat -c "SELECT * FROM machines;"        - выполнить один запрос

set DB=
for /f %%i in ('docker ps -q -f name^=factory_db') do set DB=%%i
if "%DB%"=="" (
    echo Контейнер базы данных не найден. Сначала запустите deploy.bat
    exit /b 1
)

if "%~1"=="" (
    docker exec -it %DB% psql -U factory -d factory
) else (
    docker exec %DB% psql -U factory -d factory %*
)
