@echo off
:loop
cd C:\xampp\mysql\bin\mysqldump -u root - sqlcu > backup.sql
timeout /t 1800 /nobreak > NUL
goto loop
