Build instructions
=========================

The latest release of PS3 Media Server can be downloaded from:

 - http://www.ps3mediaserver.org/

This document describes how to build PS3 Media Server from the source files.
The following software packages are required:

  - The Java JDK (JRE is not enough)
  - Git
  - Maven
  - External libraries

Read the section "Full instructions" for a complete explanation of how to
install all required software and how to build PMS for each operating system.


Short instructions
------------------

If all required software packages are installed, the following commands will
download the latest sources and build PMS:

    git clone git://github.com/ps3mediaserver/ps3mediaserver.git
    cd ps3mediaserver
    mvn package

The result will be built in the "target" directory:

    (Windows) PMS-setup.exe
    (Linux)   pms-linux-generic-x.xx.x.tar.gz
    (Mac OSX) pms-macosx-x.xx.x.dmg


Full instructions
-----------------

First all required software has to be installed:

 1. Download and install Java JDK (JRE is not enough):

    * (Windows) http://www.oracle.com/technetwork/java/javase/downloads/index.html 
        - Be sure to remember the install location. 
    * (Linux)   `sudo apt-get install sun-java6-jdk`
    * (Mac OSX) https://developer.apple.com/downloads/index.action?name=for%20Xcode%20-
	- Look for java Developer Package

 2. Download and install Git:

    * (Windows) http://code.google.com/p/msysgit/downloads/list
        - For the "Adjusting your PATH environment" section, select
          "Run Git from the Windows Command Prompt".
        - For the "Configuring the line ending conversions" section,
          select "Checkout Windows-style, commit Unix-style line endings".
    * (Linux)   `sudo apt-get install git-core git-gui git-doc`
    * (Mac OSX) http://git-scm.com/
	- If you are using brew (http://mxcl.github.com/homebrew/) you just have
	  to do 'brew install git'

 3. Download and extract Maven:

    * (Windows) http://maven.apache.org/download.html
    * (Linux)   `sudo apt-get install maven3`
    * (Mac OSX) (nothing to do, automatically installed with java for XCode in step 1)

    Be sure to remember the extract location.

 4. Set environment variables (create new variables or append the value if the
    variable already exists):

    * (Windows)
        - Level: System, variable: "JAVA_HOME", value: JDK install location
        - Level: User, variable "M2_HOME", value: Maven extract location
        - Level: User, variable "M2", value: `%M2_HOME%\bin`
        - Level: User, variable "Path", value `%M2%`
    * (Linux) (nothing to do)
    * (Mac OSX) (nothing to do)

 5. Download the PMS source code by cloning the GitHub repository:

        git clone git://github.com/ps3mediaserver/ps3mediaserver.git
        cd ps3mediaserver

 6. Resolve and install external libraries needed by the build process:

        mvn com.savage7.maven.plugins:maven-external-dependency-plugin:resolve-external
        mvn com.savage7.maven.plugins:maven-external-dependency-plugin:install-external

At this point all required software packages are present.
PMS is now ready to be built.

 7. Update to the latest source (optional):

        git pull

 8. Compile the latest version of PMS:

        mvn package

The resulting binaries will be built in the "target" directory:

    (Windows) PMS-setup.exe
    (Linux)   pms-linux-generic-x.xx.x.tar.gz
    (Mac OSX) pms-macosx-x.xx.x.dmg


Automatic builds
----------------

These last two commands can easily be automated using a script:

(Windows)

    rem build-pms.bat
    start /D ps3mediaserver /wait /b git pull
    start /D ps3mediaserver /wait /b mvn package


(Linux, Mac OSX)

    #!/bin/sh
    # build-pms.sh
    cd ps3mediaserver
    git pull
    mvn package


Instructions for developers
---------------------------

The previous sections described how to build the latest version of PMS from its
sources. For most people this will be enough to keep up to date with the latest
official developments. However, GitHub also makes it very simple for developers
to fork their own version of the official PMS sources to add their own tweaks
or features. GitHub facilitates submitting these features as "Pull Requests" to
the official PMS development team.

This section describes how to set up your own fork and how to work with it from
Eclipse (other IDEs should require similar configuration).

 * Create a GitHub account (https://github.com/).

 * Set up your machine for GitHub development (http://help.github.com/).

 * Fix the Git line endings on your machine (http://help.github.com/line-endings/).

 * Go to the GitHub PMS repo (https://github.com/ps3mediaserver/ps3mediaserver)
   and press the "Fork" button on the top right of the page to create your own
   forked repository of the official sources. 

 * Clone the new GitHub repo to your local machine. The clone URL can be seen
   on the main page of your repository. It should be something like this
   (replace YOURNAME with your actual GitHub name):
 
        git clone git@github.com:YOURNAME/ps3mediaserver.git YOURNAME

You now have the new repository on your local machine. It is time to set up an
integrated development environment to work with it. The steps below explain how
to set up Eclipse for development with Maven and Git. 

 * Download and install the Eclipse IDE for Java Developers (http://www.eclipse.org/downloads/).

 * Install the m2e Eclipse plugin (http://eclipse.org/m2e/)

 * Install the EGit Eclipse plugin (http://eclipse.org/egit/)

 * In Eclipse, select the menu "Window > Show View > Git Repositories". Then
   select "Window > Navigation > Show View Menu", choose "Add a Repository".
   Browse for the directory where you cloned your repository and press the
   "Search" button. Select your forked repository and press "OK".
   The repository should appear in the Git Repositories view.
 
 * Press the right mouse button on the repository and select "Import Maven
   Projects" from the menu. Select the project "/pom.xml" and press "Finish".

   Note: if a project with the same name already exists, click "Advanced" and
   set the "Name template" to `[artifactId]-YOURNAME` (replace YOURNAME with
   your GitHub name). Then press "Finish".

You now see the sources in Eclipse, but the project is still missing the "Git"
nature. In other words, it is not tied to the local repository yet. This means
you cannot perform any Git actions from Eclipse yet. Add the missing connection
by sharing the project:

 * Press the right mouse button on the newly created project and select the
   menu "Team > Share Project...". Select "Git" and press "Next >".
   Check the checkbox "Use or create repository in parent folder of project"
   and make sure the project is selected. Then press "Finish".

Verify that your project is now under Git control. Press the right mouse
button on the project and under "Team" you now see all options to work with
Git.

You can build PMS from Eclipse:

 * Create a new run configuration under "Run > Run Configurations...", right
   mouse button on "Maven Build", select "New", Name: `Build PMS`, Goals:
   `package`. Select the tab "JRE" and add the following VM arguments
   `-Xmx1500m -XX:MaxPermSize=256m`. Finally, press the "Apply" button.

You will want to run PMS from Eclipse while developing. This is how you do it:

 * Create a new run configuration under "Run > Run Configurations...", right
   mouse button on "Maven Build", select "New", Name: `Run PMS`, Base
   directory: `${project_loc}`, Goals: "test", Profiles: `run-from-eclipse`.
   Select the tab "JRE" and add VM arguments `-Xmx1500m -XX:MaxPermSize=256m`.
   Finally, press the "Apply" button.

You are now ready to start developing!

When you are happy with your changes, you can commit them to your local
repository from Eclipse using right mouse button, "Team > Commit...".

When you are satisfied with your commits and want to publish them to your
repository at GitHub, you can press the right mouse button on the project and
select "Team > Push to Upstream".

If you would like to contribute to the PMS project, you can send a "Pull
Request" to the development team. See the help on GitHub for more details
(http://help.github.com/send-pull-requests/).

