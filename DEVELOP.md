# Development

## Instructions for developers

The [build instructions](https://github.com/ps3mediaserver/ps3mediaserver/blob/master/BUILD.md)
describe how to build the latest version of PMS from its
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

## Line endings

If you plan to commit source code, be sure to configure git to deal properly with
cross platform line endings.

On Mac OS X and Linux:

    git config --global core.autocrlf input

On Windows:

    git config --global core.autocrlf true

For more information, see http://help.github.com/line-endings/

See [BUILD.md](https://github.com/ps3mediaserver/ps3mediaserver/blob/master/BUILD.md)
for instructions on how to set up your local development environment.

## Thanks and acknowledgements

Thanks to the following developers and companies for providing tools used in PMS development:

* [DocToc](https://github.com/thlorenz/doctoc) - a tool used to generate TOCs for some of our Markdown documents
* ej-technologies for [JProfiler](http://www.ej-technologies.com/products/jprofiler/overview.html) - Java Profiler [Open Source License](http://www.ej-technologies.com/buy/jprofiler/openSource/enter)
* JetBrains for [IntelliJ IDEA](https://www.jetbrains.com/idea/) - Java IDE [Open Source Project Development License](https://www.jetbrains.com/idea/opensource/license.html)
* [IzPack](http://izpack.org/) - used to package the Mac OS X application
