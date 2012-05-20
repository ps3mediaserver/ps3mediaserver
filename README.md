PS3 Media Server
================

by shagrath

PS3 Media Server is a cross-platform DLNA-compliant UPnP Media Server.
Originally written to support the PlayStation 3, PS3 Media Server has been
expanded to support a range of other media renderers, including smartphones,
televisions, music players and more.

Links
-----

* Website:       http://www.ps3mediaserver.org/
* Source code:   https://github.com/ps3mediaserver/ps3mediaserver
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
* happy.neko

for major code contributions.

Thanks to:

* meskibob
* otmanix

for documentation and contributions to the community.

* boblinds and snoots for the network test cases :)
* sarraken, bleuecinephile, bd.azerty, fabounnet for the support and feedback

See the `CHANGELOG` for more thanks.

Building
------------

PMS can be built using the following commands:

    git clone git://github.com/ps3mediaserver/ps3mediaserver.git
    cd ps3mediaserver
    mvn com.savage7.maven.plugins:maven-external-dependency-plugin:resolve-external
    mvn com.savage7.maven.plugins:maven-external-dependency-plugin:install-external
    mvn package

See [BUILD.md](https://github.com/ps3mediaserver/ps3mediaserver/blob/master/BUILD.md) for more extensive documentation.

Development
-----------

If you plan to commit source code, be sure to configure git to deal properly with
cross platform line endings.

On Mac OS X and Linux:

    git config --global core.autocrlf input

On Windows:

    git config --global core.autocrlf true

For more information, see http://help.github.com/line-endings/

See [BUILD.md](https://github.com/ps3mediaserver/ps3mediaserver/blob/master/BUILD.md) for instructions on how to set up your local development
environment.

