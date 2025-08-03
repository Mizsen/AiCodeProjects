@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: 基础配置
set "JDK_PATH=E:\Windows10\Java64\jdk-21"
set "PROJECT_DIR=%~dp0"
set "JAR_PATH=%PROJECT_DIR%target\blog-1.0.0.jar"
set "OUTPUT_DIR=T:\output"
set "APP_NAME=blog-system"

:: 验证JAR文件
if not exist "%JAR_PATH%" (
    echo [错误] JAR文件不存在: "%JAR_PATH%"
    pause
    exit /b 1
)

:: 步骤1：分析模块依赖
echo [STEP 1] 分析模块依赖

:: 


for /f "delims=" %%m in ('%JDK_PATH%\bin\jdeps --ignore-missing-deps -q --print-module-deps %JAR_PATH%') do (
    set "MODULES=%%m"
    echo [DEBUG] 模块: !MODULES!
)
echo [DEBUG] 检测到的模块: !MODULES!
::     --add-modules "!MODULES!,java.xml,java.desktop,java.management,java.sql,java.naming,jdk.unsupported" ^
:: java.se


for /f "delims=" %%m in ('%JDK_PATH%\bin\java --list-modules') do (
    set "ALLMODULES=%%m"
    echo [DEBUG] 模块: !ALLMODULES!
)

set "ALL_MODULES_21=java.base,java.compiler,java.datatransfer,java.desktop,java.instrument,java.logging,java.management,java.management.rmi,java.naming,java.net.http,java.prefs,java.rmi,java.scripting,java.se,java.security.jgss,java.security.sasl,java.smartcardio,java.sql,java.sql.rowset,java.transaction.xa,java.xml,java.xml.crypto,jdk.accessibility,jdk.attach,jdk.charsets,jdk.compiler,jdk.crypto.cryptoki,jdk.crypto.ec,jdk.crypto.mscapi,jdk.dynalink,jdk.editpad,jdk.hotspot.agent,jdk.httpserver,jdk.incubator.vector,jdk.internal.ed,jdk.internal.jvmstat,jdk.internal.le,jdk.internal.opt,jdk.internal.vm.ci,jdk.internal.vm.compiler,jdk.internal.vm.compiler.management,jdk.jartool,jdk.javadoc,jdk.jcmd,jdk.jconsole,jdk.jdeps,jdk.jdi,jdk.jdwp.agent,jdk.jfr,jdk.jlink,jdk.jpackage,jdk.jshell,jdk.jsobject,jdk.jstatd,jdk.localedata,jdk.management,jdk.management.agent,jdk.management.jfr,jdk.naming.dns,jdk.naming.rmi,jdk.net,jdk.nio.mapmode,jdk.random,jdk.sctp,jdk.security.auth,jdk.security.jgss,jdk.unsupported,jdk.unsupported.desktop,jdk.xml.dom,jdk.zipfs"


:: 步骤2：构建精简JRE
echo [STEP 2] 构建精简JRE
if exist "%OUTPUT_DIR%\jre" rd /s /q "%OUTPUT_DIR%\jre"
"%JDK_PATH%\bin\jlink" ^
    --module-path "%JDK_PATH%\jmods" ^
    --add-modules %ALL_MODULES_21% ^
    --strip-debug ^
    --no-man-pages ^
    --no-header-files ^
    --output "%OUTPUT_DIR%\jre"

:: 步骤3：打包EXE
echo [STEP 3] 生成EXE文件
if exist "%OUTPUT_DIR%\%APP_NAME%" rd /s /q "%OUTPUT_DIR%\%APP_NAME%"
"%JDK_PATH%\bin\jpackage" ^
    --type exe ^
    --name "%APP_NAME%" ^
    --input "%PROJECT_DIR%target" ^
    --main-jar blog-1.0.0.jar ^
    --runtime-image "%OUTPUT_DIR%\jre" ^
    --dest "%OUTPUT_DIR%" ^
    --win-console

echo [成功] 打包完成！输出目录: "%OUTPUT_DIR%"
pause