# BUILD PMS BINARIES

These scripts are only meant for enthusiasts that want to bundle their PMS with custom built versions of libraries and tools, replacing the standard versions shipped with the regular PMS distribution.

## Downloading (and updating) sources

    contrib/download-pms-binaries-source.sh

Sources archives will be strored in _target/bin-tools/src/_

## Building bainaries

    contrib/build-pms-binaries.sh

Search _target/bin-tools/target/bin/_ for compiled binaries and _target/bin-tools/target/lib/_ for libs

## Cleaning up

    rm -rf target/bin-tools/build/
    rm -rf target/bin-tools/target/

