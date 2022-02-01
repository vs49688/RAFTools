# RAFTools

A viewing/extraction toolkit for League of Legends RAF files.

### NOTICE

League of Legends has not used RAF files for some time now and as such,
RAFTools is in maintenance mode. It will not receive any new feature
development or bug fixes.

Please use https://github.com/Crauzer/Obsidian to handle the new format.

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
java -jar raftools-0.6.1.jar
```

To run as a CLI application:
```
java -jar raftools-0.6.1.jar -console
```

### Building

```
$ mvn package
$ java -jar target/raftools-0.6.1.jar
```

### License
GPLv2 Only - See the **COPYING** file for exact license details.

