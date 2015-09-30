# RAFTools

A viewing/extraction toolkit for League of Legends RAF files.

### Features
 * Automated mounting of RAF files (See the filesystem as LoL does)
 * Manual "patch" mounting
 * DDS Viewing/Exporting (including Mipmaps)
 * Preliminary Wwise Soundbank extraction
 * CLI and GUI interfaces

### Usage
Usage is pretty straightforward:

To run normally (as a GUI application):
```
java -jar RAFTools.jar
```

To run as a CLI application:
```
java -jar RAFTools.jar -console
```

### Building
RAFTools uses gradle for its build system, so the usual gradle commands will suffice:

##### Compiling
```
/path/to/RAFTools$ gradle build
```

##### Running
```
/path/to/RAFTools$ gradle run
```

##### Creating a JAR file
```
/path/to/RAFTools$ gradle jar
```

### License
GPLv2 Only - See the **COPYING** file for exact license details.
