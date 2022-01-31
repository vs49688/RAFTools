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
java -jar raftools-0.6.0.jar
```

To run as a CLI application:
```
java -jar raftools-0.6.0.jar -console
```

### Building

```
$ mvn package
$ java -jar target/raftools-0.6.0.jar
```

### License
GPLv2 Only - See the **COPYING** file for exact license details.

