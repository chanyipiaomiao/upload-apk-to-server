@echo off

if "%1" == "" (
    java -jar upload-package.jar
) else (
    java -jar upload-package.jar %1 %2 %3 %4 %5 %6 %7 %8 %9
)

pause