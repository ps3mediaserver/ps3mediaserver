#!/bin/bash
#
# This script will fire up the izpack installer using the Java Runtime
# Environment that is provided in the PlugIns directory.
#

DIRNAME=`dirname "$0"`

APPLICATION_DIR=`cd "$DIRNAME"/../..; pwd`
JAR_DIR="$APPLICATION_DIR/Contents/Resources/Java"

# Determine the name of the JRE directory
JRE_DIR=`ls "$APPLICATION_DIR/Contents/PlugIns"*`
JAVA_HOME="$APPLICATION_DIR/Contents/PlugIns/$JRE_DIR/Contents/Home"
export JAVA_HOME

# Explicitly cd to the working directory
cd "$JAVA_HOME"

# Execute the JVM
"$JAVA_HOME/bin/java" $JAVA_OPTS \
	-Xdock:icon="$APPLICATION_DIR/Contents/Resources/package.icns" \
	-classpath "$JAR_DIR/*" \
	com.izforge.izpack.installer.bootstrap.Installer


