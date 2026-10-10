@echo off
echo ========================================================
echo       Launching NAMITHAMART Full-Stack Platform...
echo ========================================================

if not exist bin\com\namithamart\Main.class (
    echo [INFO] Binaries not found. Running build.bat first...
    call build.bat
)

echo Starting NAMITHAMART Server on port 8080...
java -cp "bin;lib\gson.jar;lib\sqlite-jdbc.jar;lib\slf4j-api.jar;lib\slf4j-simple.jar" com.namithamart.Main 8080
