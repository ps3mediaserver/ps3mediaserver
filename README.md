PS3 Media Server
================

by shagrath

PS3 Media Server is a cross-platform DLNA-compliant UPnP Media Server.
Originally written to support the PlayStation 3, PS3 Media Server has been
expanded to support a range of other media renderers, including smartphones,
televisions, music players and more.

Notice
------

We are currently in the process of moving the source from Google Code to GitHub.
At the same time the source is modified to be built with Maven instead of Ant.
As a result, the source at GitHub is currently under development and possibly unstable;
use it at your own risk.

You will find the most recent stable PMS source code on Google Code:

* http://code.google.com/p/ps3mediaserver/

Links
-----

* Website:       http://www.ps3mediaserver.org/
* Issue tracker: https://code.google.com/p/ps3mediaserver/issues/list

Thanks
------

Thanks to:

* Redlum
* tcox
* SubJunk
* taconaut
* tomeko
* chocolateboy
* ditlew
* Raptor399
* renszarv

for major code contributions.

Thanks to:

* meskibob
* otmanix

for documentation and contributions to the community.

* boblinds and snoots for the network test cases :)
* sarraken, bleuecinephile, bd.azerty, fabounnet for the support and feedback

See the `CHANGELOG` for more thanks.

Installation
------------

PMS can be built using the following commands:

    git clone git://github.com/ps3mediaserver/ps3mediaserver.git
    cd ps3mediaserver
    mvn com.savage7.maven.plugins:maven-external-dependency-plugin:resolve-external
    mvn com.savage7.maven.plugins:maven-external-dependency-plugin:install-external
    mvn clean package

Development
-----------

If you plan to commit source code, be sure to configure git to deal properly with
cross platform line endings.

On Mac OS X and Linux:

    git config --global core.autocrlf input

On Windows:

    git config --global core.autocrlf true

For more information, see http://help.github.com/line-endings/

