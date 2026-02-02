@echo off
cd /d "%~dp0"
echo Compiling Java files...
javac -d bin -cp "bin;lib/*" src\auth\*.java src\course\*.java src\educator\*.java src\forum\*.java src\main\*.java src\quiz\*.java src\student\*.java src\util\*.java src\gui\*.java

echo.
echo Running Login GUI...
java -cp "bin;lib/*" gui.LoginGUI

pause
