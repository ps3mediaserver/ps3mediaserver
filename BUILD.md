## Table of Contents

- [Build instructions](#build-instructions)
- [Short instructions](#short-instructions)
- [Full instructions](#full-instructions)
	- [1. Download and install the Java JDK](#1-download-and-install-the-java-jdk)
		- [Windows](#windows)
		- [Linux](#linux)
		- [Mac OS X](#mac-os-x)
	- [2. Download and install Git](#2-download-and-install-git)
		- [Windows](#windows-1)
		- [Linux](#linux-1)
		- [Mac OS X](#mac-os-x-1)
	- [3. Download and extract Maven](#3-download-and-extract-maven)
		- [Windows](#windows-2)
		- [Linux](#linux-2)
		- [Mac OS X](#mac-os-x-2)
	- [4. Set environment variables](#4-set-environment-variables)
		- [Windows](#windows-3)
		- [Linux](#linux-3)
		- [Mac OS X](#mac-os-x-3)
	- [5. Download the PMS source code](#5-download-the-pms-source-code)
	- [6. Resolve and install external libraries](#6-resolve-and-install-external-libraries)
	- [7. Update to the latest source (optional)](#7-update-to-the-latest-source-optional)
	- [8. Compile the latest version of PMS](#8-compile-the-latest-version-of-pms)
	- [Automatic builds](#automatic-builds)
		- [Windows](#windows-4)
		- [Linux, Mac OS X &c.](#linux-mac-os-x-&c)
- [Cross-compilation](#cross-compilation)
	- [Building the Windows binaries](#building-the-windows-binaries)
		- [On Linux](#on-linux)
		- [On Mac OS X](#on-mac-os-x)
	- [Building the Linux tarball](#building-the-linux-tarball)
		- [On Windows](#on-windows)
		- [On Mac OS X](#on-mac-os-x-1)
	- [Building the Mac DMG file](#building-the-mac-dmg-file)
- [Instructions for developers](#instructions-for-developers)

# Build instructions

The latest release of PS3 Media Server can be downloaded from: http://www.ps3mediaserver.org/

This document describes how to build PS3 Media Server from the source files.
The following software packages are required:

* The Java JDK (the JRE is not enough)
* Git
* Maven
* External libraries

Read the [Full instructions](#full-instructions) section for a complete explanation of how to
install all required software and how to build PMS for each operating system.

# Short instructions

If all required software packages are installed, the following commands will
download the latest sources and build PMS:

    git clone git://github.com/ps3mediaserver/ps3mediaserver.git
    cd ps3mediaserver
    mvn package

The result will be built in the "target" directory:

* Windows: `PMS-setup-full.exe`, `PMS-setup-full-x64.exe` and `PMS-setup-without-jre.exe`
* Linux: `pms-linux-generic-x.xx.x.tar.gz`
* Mac OS X: `pms-setup-macosx-x.xx.x.tar.gz`

# Full instructions

First all required software has to be installed:

## 1. Download and install the Java JDK

Note: the JRE is not enough.

### Windows

See http://www.oracle.com/technetwork/java/javase/downloads/index.html

Be sure to remember the install location.

### Linux

    sudo apt-get install openjdk-7-jdk

### Mac OS X

See https://developer.apple.com/downloads/index.action?name=for%20Xcode%20-

Look for the Java Developer Package.

## 2. Download and install Git

### Windows

See http://code.google.com/p/msysgit/downloads/list

For the "Adjusting your PATH environment" section,
select "Run Git from the Windows Command Prompt".

For the "Configuring the line ending conversions" section,
select "Checkout Windows-style, commit Unix-style line endings".

### Linux

    sudo apt-get install git-core git-gui git-doc

### Mac OS X

See http://git-scm.com/

If you are using brew (http://mxcl.github.com/homebrew/) you just have
to do:

    brew install git

## 3. Download and extract Maven

### Windows

See http://maven.apache.org/download.html

### Linux

    sudo apt-get install maven3

### Mac OS X

Nothing to do, automatically installed with Java for XCode in step 1.

Be sure to remember the extract location.

## 4. Set environment variables

### Windows

Create new variables or append the value if the variable already exists:

* Level: System, variable: `JAVA_HOME`, value: JDK install location
* Level: User, variable `M2_HOME`, value: Maven extract location
* Level: User, variable `M2`, value: `%M2_HOME%\bin`
* Level: User, variable `PATH`, value `%M2%`

### Linux

Nothing to do.

### Mac OS X

Nothing to do.

## 5. Download the PMS source code

    git clone git://github.com/ps3mediaserver/ps3mediaserver.git
    cd ps3mediaserver

## 6. Resolve and install external libraries

These are needed by the build process:

    mvn com.savage7.maven.plugins:maven-external-dependency-plugin:resolve-external
    mvn com.savage7.maven.plugins:maven-external-dependency-plugin:install-external

At this point all required software packages are present.
PMS is now ready to be built.

## 7. Update to the latest source (optional)

    git pull

## 8. Compile the latest version of PMS

    mvn package

The resulting binaries will be built in the "target" directory:

* Windows: `PMS-setup-full.exe`, `PMS-setup-full-x64.exe` and `PMS-setup-without-jre.exe`
* Linux:   `pms-linux-generic-x.xx.x.tar.gz`
* Mac OS X: `pms-setup-macosx-x.xx.x.tar.gz`

## Automatic builds

These last two commands can easily be automated using a script e.g.:

### Windows

    rem build-pms.bat
    start /D ps3mediaserver /wait /b git pull
    start /D ps3mediaserver /wait /b mvn package

### Linux, Mac OS X &c.

    #!/bin/sh
    # build-pms.sh
    cd ps3mediaserver
    git pull
    mvn package

# Cross-compilation

By default, `mvn package` builds an installer or distibution file for the
platform it is being compiled on e.g. `PMS-setup-full.exe` on Windows and a tarball on Linux.

As an optional step, releases for other platforms can be built.

## Building the Windows binaries

The Windows installers (`PMS-setup-full.exe`, `PMS-setup-full-x64.exe` and `PMS-setup-without-jre.exe`) and Windows executable
(`PMS.exe`) can be built on non-Windows platforms.

First of all, you'll need to have the `makensis` binary installed. On Debian/Ubuntu,
this can be done with:

    sudo apt-get install nsis

Then the `NSISDIR` environment needs to be set to the **absolute path** to the
`nsis` directory. This can either be set per-command:

    NSISDIR=$PWD/src/main/external-resources/third-party/nsis mvn ...

\- temporarily in the current shell:

    export NSISDIR=$PWD/src/main/external-resources/third-party/nsis
    mvn ...

\- or permanently:

    # these two commands only need to be run once
    echo "export NSISDIR=$PWD/src/main/external-resources/third-party/nsis" >> ~/.bashrc
    source ~/.bashrc

    mvn...

For the sake of brevity, the following examples assume it has already been set.

The Windows installer can now be built with one of the following commands:

### On Linux

    mvn package -P system-makensis,windows

### On Mac OS X

    mvn package -P system-makensis,windows,-osx

## Building the Linux tarball

### On Windows

    mvn package -P linux,-windows

### On Mac OS X

    mvn package -P linux,-osx

## Building the Mac OS X installer tarball

The Mac OS X installer tarball can be built on any platform by specifying
the "osx" profile explicity:

    mvn package -P osx

# Instructions for developers

Now that you have a build environment set up, you can start hacking on
PMS. Instructions and tips for developing/patching PMS can be found
[here](https://github.com/ps3mediaserver/ps3mediaserver/blob/master/DEVELOP.md).
