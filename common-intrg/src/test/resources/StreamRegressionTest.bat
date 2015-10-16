cls
@echo on

REM Server Drive where the Jar file is kept
set BATCH_RUN_DESTINATION=C:\ClaimStream
set JAVA_HOME=C:\jdk1.6.0_21\bin\java
set BATCH_INVOKER=com.client.iip.integration.RegressionTestProcessor
set CP=%BATCH_RUN_DESTINATION%\resources;%BATCH_RUN_DESTINATION%\common-iip-client.jar

FOR /R ./lib %%a in (*.jar) DO CALL :AddToPath %%a


REM Launch the Regression Test
	%JAVA_HOME% -cp %CP% %BATCH_INVOKER%

pause

:AddToPath
SET CP=%1;%CP%
GOTO :EOF

