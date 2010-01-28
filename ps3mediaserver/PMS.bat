@echo off
echo Java PS3 Media Server
echo ---------------------
echo In case of troubles with PMS.exe, this shell will launch ps3mediaserver in a more old fashioned way
echo You can try to reduce the Xmx parameter value if you keep getting "Cannot create Java virtual machine" errors...
echo Last word: You must have java installed ! http://www.java.com
echo ------------------------------------------------
pause
start javaw -Xmx768M -Djava.net.preferIPv4Stack=true -Djava.encoding=UTF-8 -classpath update.jar;pms.jar;plugins/*;plugins net.pms.PMS
