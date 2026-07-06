@echo off
title Teleport Web
start http://127.0.0.1:8765/
call "%~dp0START_SERVER.bat"
