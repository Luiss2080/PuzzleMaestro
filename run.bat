@echo off
echo Compilando Puzzle 4x4...
javac -d bin src\logica\*.java src\main\*.java src\presentacion\*.java

if %ERRORLEVEL% NEQ 0 (
    echo Error de compilacion!
    pause
    exit /b 1
)

echo Iniciando Puzzle 4x4...
java -cp bin main.Main

pause
