@echo off
setlocal enabledelayedexpansion

set "JDK_PATH=E:\Windows10\Java64\jdk-21"
set "JAR_PATH=T:\output\blog-1.0.0.jar"

for /f "delims=" %%m in ('%JDK_PATH%\bin\jdeps --ignore-missing-deps -q --print-module-deps %JAR_PATH%') do (
    set "MODULES=%%m"
    echo [DEBUG] Ä£¿é: !MODULES!
)



for /f "delims=" %%m in ('%JDK_PATH%\bin\java --list-modules') do (
    set "ALLMODULES=%%m"
    echo [DEBUG] ALLMODULESÄ£¿é: !ALLMODULES!
)


echo [DEBUG] Ä£¿é: !ALLMODULES!