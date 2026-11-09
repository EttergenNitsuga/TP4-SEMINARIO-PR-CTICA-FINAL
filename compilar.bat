@echo off
setlocal
chcp 65001 >nul
if not exist out mkdir out
javac -Xlint:all -encoding UTF-8 -d out src\ar\edu\hornero\*.java
if errorlevel 1 (
  echo.
  echo ERROR: no se pudo compilar. Verifica que JDK 17 o superior este instalado.
  exit /b 1
)
echo.
echo Compilacion finalizada correctamente.
exit /b 0
