@echo off
echo ========================================================
echo       NAMITHAMART Full-Stack Platform Build Script
echo ========================================================

if not exist bin mkdir bin

echo Compiling Java source files...
java -cp "lib\ecj.jar;lib\gson.jar;lib\sqlite-jdbc.jar;lib\slf4j-api.jar;lib\slf4j-simple.jar" org.eclipse.jdt.internal.compiler.batch.Main -1.8 -d bin -cp "lib\gson.jar;lib\sqlite-jdbc.jar;lib\slf4j-api.jar;lib\slf4j-simple.jar" src\main\java\com\namithamart\Main.java src\main\java\com\namithamart\Database.java src\main\java\com\namithamart\models\*.java src\main\java\com\namithamart\utils\*.java src\main\java\com\namithamart\controllers\*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build failed! Check errors above.
    exit /b %ERRORLEVEL%
)

echo [SUCCESS] NAMITHAMART built successfully into bin/!
echo Run "run.bat" to start the server.
