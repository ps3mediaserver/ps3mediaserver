# Development

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
