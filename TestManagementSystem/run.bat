@echo off
cd /d "%~dp0"
echo Compiling Java files...
javac -d bin -cp "bin;lib/*" src\auth\*.java src\course\*.java src\educator\*.java src\forum\*.java src\main\*.java src\quiz\*.java src\student\*.java src\util\*.java

echo.
echo Running Test Management System...
java -cp "bin;lib/*" main.Main

pause
