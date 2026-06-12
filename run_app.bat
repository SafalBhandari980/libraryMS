@echo off
echo Starting Library Management System...
echo ========================================
echo.
cd /d "%~dp0"
java -cp "build\classes;lib\*" libraryms.LibraryMS > execution_log.txt 2>&1
echo Done. See execution_log.txt for details.
pause
