@echo off
rem
rem Copyright (c) 2006-2010 Nokia Corporation and/or its subsidiary(-ies).
rem All rights reserved.
rem This component and the accompanying materials are made available
rem under the terms of "Eclipse Public License v1.0"
rem which accompanies this distribution, and is available
rem at the URL "http://www.eclipse.org/legal/epl-v10.html".
rem
rem Initial Contributors:
rem Nokia Corporation - initial contribution.
rem
rem Contributors:
rem
rem Description:
rem
:: Find the "CarbideUI.bat" file absolute path dymamically
set BatFullPath=%~f0%
set CarbideUIBatFilePath=%BatFullPath:\CarbideUI.bat=%

:: Checking the Window Operating System for Windows XP, Windows Vista and Windows 7
ver | %WINDIR%\System32\find.exe " 5." > nul
if %ERRORLEVEL% == 0 goto Ver_XP

ver | %WINDIR%\System32\find.exe " 6." > nul
if %ERRORLEVEL% == 0 goto Ver_Vista

ver | %WINDIR%\System32\find.exe " 7." > nul
if %ERRORLEVEL% == 0 goto Ver_Win7

echo Carbide.ui Theme Edition tool is only supported on Windows XP and Windows Vista operating system.
goto exit

:: Run Carbide.ui Theme Edition for Windows XP
:Ver_Xp
:Run Windows XP-specific commands here.
cd %CarbideUIBatFilePath%
start .\eclipse\eclipse.exe -vm .\jre\bin\client\jvm.dll
goto exit

:: Run Carbide.ui Theme Edition for Windows Vista
:Ver_Vista
:Run Windows Vista-specific commands here.
cd %CarbideUIBatFilePath%
start .\eclipse\eclipse.exe -vm .\jre\bin\client\jvm.dll
goto exit

:: Run Carbide.ui Theme Edition for Windows 7
:Ver_Win7
:Run Windows 7-specific commands here.
cd %CarbideUIBatFilePath%
start .\eclipse\eclipse.exe -vm .\jre\bin\client\jvm.dll
goto exit

:exit
