@echo off
setlocal
chcp 65001 >nul
call compilar.bat
if errorlevel 1 goto fin
if not exist "lib\*.jar" (
  echo.
  echo FALTA MySQL Connector/J.
  echo Descarga el archivo JAR oficial y copialo dentro de la carpeta lib.
  echo Despues vuelve a ejecutar este archivo.
  goto fin
)
echo.
echo Iniciando Cabanas El Hornero...
java -Dfile.encoding=UTF-8 -cp "out;lib\*" ar.edu.hornero.Main
:fin
echo.
pause
