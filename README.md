# RAFTools

![](https://travis-ci.org/vs49688/RAFTools.svg?branch=master)

A viewing/extraction toolkit for League of Legends RAF files.

### Features
 * Automated mounting of RAF files (See the filesystem as LoL does)
 * Manual "patch" mounting
 * DDS Viewing/Exporting (including Mipmaps)
 * Preliminary Wwise Soundbank extraction
 * CLI and GUI interfaces

### Download
https://celestia.vs49688.net/jenkins/job/RAFTools/

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

### Donations
At request, I am providing donation information, both PayPal and Bitcoin. Please note
that any donations go to the sole contributor for the project as a way of saying _Thank you_
and to incentivise work on the project.

[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.me/vs49688)

Bitcoin: 16ozmnVZmmXjBtXDn2q63pKaMQzDRxekow