@echo off
setlocal
chcp 65001 >nul
call compilar.bat
if errorlevel 1 goto fin
echo.
echo Ejecutando pruebas del modelo, arreglos y archivos (no requiere MySQL)...
echo.
java -Dfile.encoding=UTF-8 -cp out ar.edu.hornero.PruebaModelo
:fin
echo.
pause
