@rem Gradle startup script for Windows
@if "%DEBUG%"=="" @echo off
@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set JAVA_HOME=C:\Users\TI\AppData\Local\Programs\Android Studio\jbr
set GRADLE_WRAPPER=%USERPROFILE%\.gradle\wrapper\dists\gradle-8.9-bin\90cnw93cvbtalezasaz0blq0a\gradle-8.9\bin\gradle.bat

call "%GRADLE_WRAPPER%" %*
