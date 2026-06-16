@echo off
echo ================================================
echo   RESET MYSQL ROOT PASSWORD - Run as Admin
echo ================================================
echo.

:: Check admin rights
net session >nul 2>&1
if %errorLevel% NEQ 0 (
    echo ERROR: Please right-click this file and select "Run as administrator"
    pause
    exit /b 1
)

echo [1/5] Stopping MySQL80 service...
net stop MySQL80
if %errorLevel% NEQ 0 (
    echo Failed to stop MySQL80. Trying alternative service names...
    net stop MySQL
)
timeout /t 2 /nobreak >nul

echo.
echo [2/5] Starting MySQL in skip-grant-tables mode...
start /b "" "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe" --defaults-file="C:\ProgramData\MySQL\MySQL Server 8.0\my.ini" --skip-grant-tables --skip-networking --console
timeout /t 5 /nobreak >nul

echo.
echo [3/5] Resetting root password to: PhoneStore2024
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root --connect-timeout=10 -e "FLUSH PRIVILEGES; ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'PhoneStore2024'; FLUSH PRIVILEGES;" 2>&1
if %errorLevel% NEQ 0 (
    echo First method failed, trying alternative...
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root --connect-timeout=10 -e "FLUSH PRIVILEGES; UPDATE mysql.user SET authentication_string='' WHERE User='root'; UPDATE mysql.user SET plugin='mysql_native_password' WHERE User='root'; FLUSH PRIVILEGES;" 2>&1
    timeout /t 2 /nobreak >nul
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root --connect-timeout=10 -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'PhoneStore2024'; FLUSH PRIVILEGES;" 2>&1
)

echo.
echo [4/5] Killing skip-grant-tables mysqld process...
taskkill /f /im mysqld.exe >nul 2>&1
timeout /t 3 /nobreak >nul

echo.
echo [5/5] Starting MySQL80 service normally...
net start MySQL80
timeout /t 3 /nobreak >nul

echo.
echo [TEST] Verifying new password...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -pPhoneStore2024 -e "SELECT 'PASSWORD RESET SUCCESS!' as result; CREATE DATABASE IF NOT EXISTS phonestore_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; SHOW DATABASES;" 2>&1

echo.
echo ================================================
echo   Done! New MySQL root password: PhoneStore2024
echo   Database phonestore_db created (if not exists)
echo ================================================
pause
