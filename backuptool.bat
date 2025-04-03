@echo off
:loop
mysqldump -u root -  sqlcu > backup.sql
timeout /t 1800 /nobreak > NUL
goto loop
