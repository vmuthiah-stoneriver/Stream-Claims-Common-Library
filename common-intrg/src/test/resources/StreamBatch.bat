cls
@echo on

REM ############## Environment Setup ######################
set BATCH_RUN_PATH=C:\StreamBatch
set JAVA_HOME=C:\jdk1.6.0_21\bin\java
REM ############## Environment Setup ######################
set BATCH_INVOKER=com.client.iip.integration.BatchJobLauncher
set CP=%BATCH_RUN_PATH%\resources;%BATCH_RUN_PATH%\security;%BATCH_RUN_PATH%\StreamBatch.jar

rem Grab all command line arguments at once
set CMD_LINE_ARGS=%*

FOR /R ./lib %%a in (*.jar) DO CALL :AddToPath %%a


REM Launch the Batch
%JAVA_HOME% -Dlog4j.configuration=file:\\\%BATCH_RUN_PATH%\log4j.xml -cp %CP% %BATCH_INVOKER% %CMD_LINE_ARGS%

pause

:AddToPath
SET CP=%1;%CP%
GOTO :EOF

