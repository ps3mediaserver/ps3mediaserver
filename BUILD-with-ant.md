Ant build instructions
==========================

This document describes how to build PS3 Media Server from the source files with Ant.
The following software packages are required:

  - The Java JDK (JRE is not enough)
  - Git
  - Ant
  - Ant-Contrib
  - External libraries

Read the section "Full instructions" for a complete explanation of how to
install all required software and how to build PMS for each operating system.


Short instructions
------------------

If all required software packages are installed, the following commands will
download the latest sources and build PMS:

    git clone git://github.com/ps3mediaserver/ps3mediaserver.git
    cd ps3mediaserver
    ant jar

The result will be built in the "target" directory:

    pms.jar


Full instructions
-----------------

First all required software has to be installed:

 1. Download and install Java JDK (JRE is not enough):

    * (Windows) http://www.oracle.com/technetwork/java/javase/downloads/index.html 
        - Be sure to remember the install location. 
    * (Linux)   `sudo apt-get install openjdk-6-jdk`
    * (Mac OSX) http://developer.apple.com/technologies/xcode.html

 2. Download and install Git:

    * (Windows) http://code.google.com/p/msysgit/downloads/list
        - For the "Adjusting your PATH environment" section, select
          "Run Git from the Windows Command Prompt".
        - For the "Configuring the line ending conversions" section,
          select "Checkout Windows-style, commit Unix-style line endings".
    * (Linux)   `sudo apt-get install git`
    * (Mac OSX) http://git-scm.com/

 3. Download and install Ant and Ant-Contrib:

    * (Windows) <FIXME>
    * (Linux)   `sudo apt-get install ant ant-contrib`
    * (Mac OSX) (nothing to do, automatically installed with XCode in step 1)

 4. Set environment variables (create new variables or append the value if the
    variable already exists):

    * (Windows) <FIXME>
    * (Linux) (nothing to do)
    * (Mac OSX) (nothing to do)

 5. Download the PMS source code by cloning the GitHub repository:

        git clone git://github.com/ps3mediaserver/ps3mediaserver.git
        cd ps3mediaserver

 6. (optional) Update to the latest source if your source tree is out of date:

        git pull

 7. (optional) Resolve and install external libraries needed by the build process:

        ant get-deps-all

At this point all required software packages are present in "target/ant-dependencies" directory.
PMS is now ready to be built.

 8. Compile the latest version of PMS:

        ant jar

or if you want to build in offline mode using prevously downloaded dependencies:

        ant jar -Dmaven.settings.offline=true

The resulting binary will be built in the "target" directory:

    pms.jar


Information for developers
---------------------------

Ant compiling is designed to be fallback method for build enviroments where Maven 3
is unavailable or offline builds are required.

You can overwrite `maven-build.properties` and `maven-build.xml` with

        mvn ant:ant

to apply `pom.xml` changes automaticaly. 

`build.xml` and `pms-build.properties` should be edited manually.
