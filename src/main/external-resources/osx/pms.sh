#!/bin/bash
#
# This script will fire up PS3 Media Server using the Java Runtime
# Environment that is provided in the PlugIns directory.
#

# Set this to true if you want to hide the dock icon by default
hide_dock_icon=false

DIRNAME=`dirname "$0"`

# By default Mac OS X LC_ALL is set to "C", which means files with special characters will not be found.
# http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4733494
# http://stackoverflow.com/questions/7165108/in-osx-lion-lang-is-not-set-to-utf8-how-fix
export LC_ALL="en_US.UTF-8"

APPLICATION_DIR=`cd "$DIRNAME"/../..; pwd`
JAR_DIR="$APPLICATION_DIR/Contents/Resources/Java"

# Determine the name of the JRE directory
JRE_DIR=`ls "$APPLICATION_DIR/Contents/PlugIns"*`
JAVA_HOME="$APPLICATION_DIR/Contents/PlugIns/$JRE_DIR/Contents/Home"
export JAVA_HOME

# Setup the classpath
PMS_JARS="$JAR_DIR/pms.jar:$JAR_DIR/lib/*"

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
	-Dapple.awt.UIElement="$hide_dock_icon" \
	-Djava.library.path="." \
	-classpath "$PMS_JARS" \
	net.pms.PMS

