@echo off
echo PS3 Media Server MLX
echo ---------------------
echo In case of troubles with PMS.exe, this shell will launch ps3mediaserver in a more old fashioned way
echo You can try to reduce the Xmx parameter value if you keep getting "Cannot create Java virtual machine" errors...
echo Last word: You must have java installed ! http://www.java.com
echo ------------------------------------------------
pause
start javaw -classpath update.jar;pms.jar -Xmx1024M -Dsun.java2d.d3d=false -Dfile.encoding=UTF-8
