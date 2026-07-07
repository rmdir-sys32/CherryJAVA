@echo off
javac -d bin *.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    exit /b %errorlevel%
)
java -cp bin Main
