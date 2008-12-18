#!/bin/sh

DIRNAME=`dirname $0`


# OS specific support (must be 'true' or 'false').
cygwin=false;
case "`uname`" in
CYGWIN*)
cygwin=true
;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
[ -n "$PMS_HOME" ] &&
PMS_HOME=`cygpath --unix "$PMS_HOME"`
[ -n "$JAVA_HOME" ] &&
JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi

# Setup PMS_HOME
if [ "x$PMS_HOME" = "x" ]; then
	PMS_HOME=`cd $DIRNAME/; pwd`
fi
export PMS_HOME

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
	if [ "x$JAVA_HOME" != "x" ]; then
		JAVA="$JAVA_HOME/bin/java"
	else
		JAVA="java"
	fi
fi

# Setup the classpath
PMS_CLASSPATH=".:$PMS_HOME/lib/pms.jar:$PMS_HOME/lib/commons-logging-api-1.0.4.jar:$PMS_HOME/lib/nanoxml-2.2.3.jar:$PMS_HOME/lib/java-unrar-0.2.jar:$PMS_HOME/lib/entagged-cvs.jar:$PMS_HOME/lib/forms-1.2.1.jar:$PMS_HOME/lib/looks-2.2.1.jar:$PMS_HOME/lib/jna.jar"

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
	PMS_HOME=`cygpath --path --windows "$PMS_HOME"`
	JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
	PMS_CLASSPATH=`cygpath --path --windows "$PMS_CLASSPATH"`
fi

# Execute the JVM
exec "$JAVA" $JAVA_OPTS -Xmx768M -Djava.encoding=UTF-8 -classpath $PMS_CLASSPATH net.pms.PMS "$@"

