@echo off
if not "%1" == "" (
  echo Creating self-signed test certificate...
  "c:\Program Files\Java\jre-10.0.1\bin\keytool.exe" -genkeypair -alias "%1" -keyalg RSA -keystore keystore.jks
) else (
  echo Specify alias/domain as the first and only argument.
)
