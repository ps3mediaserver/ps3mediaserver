# PS3 Media Server Plugin Development

## Basics
PS3 Media Server Plugin is a self-contained .jar which is loaded during the runtime of
PS3 Media Server (PMS). PMS checks the ```plugins/``` folder from it's root and loads the
correctly constructed jars. From the PMS trace you can find a line about trying to load
plugins from the folder, e.g. ```Searching for plugins in /xxx/xxx/plugins```

By default PMS doesn't load all jars from the plugins/ folder. The jar must contain a text file named
```plugin``` in it's root, which contains the package and class of your plugin interface
(contained in the jar). Example plugin file contains one line and looks like this: 
```com.glebb.helloworld.Plugin``` The class Plugin must implement a PMS plugin interface
ExternalListener or extend some other class implementing it. If these two conditions are satisfied,
PMS will try to load the plugin.

## Getting started
Here's a step-by-step guide to start developing plugins for PMS.
You can develop using whatever tools you like, but in this guide we are using Gradle build system
and Eclipse IDE.

Prerequisites:
   * You have build a snapshot PMS as described in [instructions](https://github.com/ps3mediaserver/ps3mediaserver/blob/master/BUILD.md) and you have set up the development environment.
   * [Gradle](http://www.gradle.org/) is installed and working
   
### Step 1: install PMS to local maven repository
By doing this you don't have to manually load PMS-jars while developing your plugin.
In PMS root folder, execute: ```mvn javadoc:jar source:jar install```. The ```javadoc:jar source:jar``` -parameters
tell maven to install additional jars to local repository, containing source and javadocs, which makes it
easier to develop the plugins using an IDE like Eclipse.

NOTICE: maven plugins to create javadoc and source jars was included in PMS git commit ```0c5414c2bf```. If you are using
older version, please update, or add the plugins manually to pom.xml and run the mvn command after that. You can also
omit the "javadoc:jar source:jar" parameters, but then you will not be able to jump to the source in the IDE or
see the javadocs.

### Step 2: set up project using Gradle
   1. Create a folder for your plugin project
   2. Create a build.gradle file there. Content: 
   ```
    apply plugin: 'java'
    apply plugin: 'application'
    apply plugin: 'eclipse'

    
    mainClassName = "com.glebb.helloworld.Plugin"
    
    defaultTasks 'clean', 'jar'
    
    version = '1.0.0'
    jar.baseName = 'helloworld' // otherwise it defaults to the directory name
    
    repositories {
        mavenLocal()
        mavenCentral()
    }
    
    dependencies {
        compile group: 'net.pms', name: 'pms', version: '1.5+'
    }
   ```
   The content is pretty bare-bone and self-explaining. The dependencies declaration tries to load
   PMS jars from first local and then mavenCentral repository. You might need to tweak the version. Also notice
   the mainClassName, which you want to change later to reflect the main Plugin class you implement.
   3. Execute ```gradle``` to see if it works. If everything goes as planned you should see "BUILD SUCCESSFUL".
   4. Execute ```gradle eclipse``` to create files needed to load the project in Eclipse

### Step 3: Configure project in Eclipse
   1. Import the project in Eclipse workspace (General / existing project)
   2. Create source folders (BuildPath/Configure Build Path.../Source):
   ```
    src/main/java
    
    src/main/resources
    
    src/main/tests
   ```
   These are default locations where Gradle tries to load the source and resource files. You can use
   different folders, but you need to set up gradle.build accordingly.
   
### Step 4: Implement Skeleton-Plugin
   1. Create a class (make sure you define the same class as main class in build.gradle) to src/main/java
   2. Make the class implement ```net.pms.external.ExternalListener``` (Gradle should have added the PMS dependency to your project
   automatically, so the class should be resolvable by default)
   3. Add unimplemented methods
   4. Implement name method: ```return "HelloWorld Plugin";```
   5. Create a new file called "plugin" to src/main/resources
   6. Write a single line e.g. ```com.glebb.helloworld.Plugin``` to the plugin-file. This needs to be the main class of your plugin with the package.
   7. Execute ```gradle```
   8. Check that ```build/libs/helloworld-1.0.0.jar``` is created. (If you later run into problems, you can check the jar and make sure the root contains the plugin file with correct path to mainClass, which should also be included)

### Step 5: Load the plugin in PMS
   1. Copy your created jar (e.g. helloworld-1.0.0.jar) to plugins/ folder of your PMS.
   2. Start PMS
   3. Check from Traces that plugin is loaded:
   ```
    Searching for plugins in ...
    Found plugin: com.glebb.helloworld.Plugin
   ```
   4. Plugin should also show up at the bottom of General Configuration tab.

That's it. Now you have a working project to build on, Happy plugging!
   
You can download a skeleton "HelloWorld" plugin from https://github.com/glebb/pms-helloworld-plugin which implements steps stated here.

## Plugin types provided by PMS
By implementing interfaces found from net.pms.external you can create different types of plugins.

### Example: AdditionalFolderAtRoot
Adds a new VirtualFolder to the root system. This type of plugin requires 4 methods to be implemented:
   * name, used in the plugin list
   * config, returning a JComponent object that shows any configuration options onscreen. This is being called when the plugin list button is pressed. Can return null.
   * shutdown, in case you need to clean up something, e.g. file or network handlers. Can be empty.
   * getChild, returning a VirtualFolder item. Needs to return real DLNAResource (e.g. VirtualFolder)

With this type you should also implement discoverChildren to at least on of the child classes (where you call addChild method to populate folders with actual DLNAResource file (e.g. RealFile). RefreshChildren should also be implemented, if you want to update the folders.

## Debugging plugins when running PMS from Eclipse
Unfortunately current version of PMS has some problems with debugging plugins. This will be fixed when
[pms-mlx](http://ps3mediaserver.org/forum/viewtopic.php?f=12&t=9775) will be merged into PMS. Until that, here's a workaround:

As the relative path of the plugins directory isn't the same when running from eclipse or when packaged, you have to load the plugins dir from a properites file [click](https://github.com/taconaut/pms-mlx/blob/master/src/main/java/net/pms/configuration/PmsConfiguration.java#L2181) and configure the version in a properties file to run from eclipse [click](https://github.com/taconaut/pms-mlx/blob/master/src/test/resources/project.properties#L4) and when packaged [click](https://github.com/taconaut/pms-mlx/blob/master/src/main/resources/project.properties#L10).
In ExternalFactory, specify the pms classloader as the base class loader for the one instanciating the plugins [click](https://github.com/taconaut/pms-mlx/blob/master/src/main/java/net/pms/plugins/PluginsFactory.java#L281).
Then you can put the plugins into /src/main/external-resources/plugins 

Load both, modified PMS and your plugin projects, to the same workspace and launch PMS using debug mode.

