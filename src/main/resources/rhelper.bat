@echo off
rem echo %0
setlocal
java -jar %~dp0rhelper.jar %1 %2
endlocal
pause
