#!/bin/bash
#
# This script will fire up PS3 Media Server using the Java Runtime
# Environment that is provided in the PlugIns directory.
#

DIRNAME=`dirname "$0"`

APPLICATION_DIR=`cd "$DIRNAME"/../..; pwd`
JAR_DIR="$APPLICATION_DIR/Contents/Resources/Java"

# Determine the name of the JRE directory
JRE_DIR=`ls "$APPLICATION_DIR/Contents/PlugIns"*`
JAVA_HOME="$APPLICATION_DIR/Contents/PlugIns/$JRE_DIR/Contents/Home"
export JAVA_HOME

# Setup the classpath
PMS_JARS="$JAR_DIR/pms.jar:$JAR_DIR/lib/*.jar"

PMS_HOME="$APPLICATION_DIR/Contents/Resources/Java"
export PMS_HOME

# Configure fontconfig (used by our build of ffmpeg)
if [ "x$FONTCONFIG_PATH" = "x" ]; then
	FONTCONFIG_PATH="$PMS_HOME/fonts"
	export FONTCONFIG_PATH
fi

if [ "x$FONTCONFIG_FILE" = "x" ]; then
    FONTCONFIG_FILE="$PMS_HOME/fonts/fonts.conf"
    export FONTCONFIG_FILE
fi

# Explicitly cd to the working directory
cd "$PMS_HOME"

# Execute the JVM
"$JAVA_HOME/bin/java" $JAVA_OPTS \
	-Xmx768M -Xss1024k -Dfile.encoding=UTF-8 -Djava.net.preferIPv4Stack=true \
	-Xdock:icon="$APPLICATION_DIR/Contents/Resources/pms.icns" \
	-classpath "$PMS_JARS" \
	net.pms.PMS

