#!/bin/bash
#
# This script will copy the Java Runtime Environment shipped with the
# installer to the PS3 Media Server application directory
#

DIRNAME=`dirname "$0"`
SOURCE_DIR=`cd "$JAVA_HOME"/../../../..; pwd`

# The installer will pass on the JAVA_HOME as an argument, see "install.xml"
# Alas, the installer does not surround args with double quotes, so reconstructive
# surgery is needed to recover the full path as it probably contains spaces.
JAVA_HOME=
separator=

for i in "$@"; do
    JAVA_HOME="${JAVA_HOME}${separator}${i}"
    separator=" "
done

echo "$JAVA_HOME" > /Users/patricka/test.txt

TARGET_DIR=`cd "$DIRNAME"/..; pwd`

# Copy the entire JRE directory
cp -rf "${SOURCE_DIR}/PlugIns" "$TARGET_DIR/"
