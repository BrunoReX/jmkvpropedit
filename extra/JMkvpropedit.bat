@ECHO OFF

CD /D "%~dp0"
start javaw -jar JMkvpropedit.jar

IF '%ERRORLEVEL%'=='0' GOTO :OK
PAUSE

:OK
EXIT