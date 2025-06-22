[![Build Status](https://github.com/centic9/commons-dost/actions/workflows/gradle-build.yml/badge.svg)](https://github.com/centic9/commons-dost/actions)
[![Gradle Status](https://gradleupdate.appspot.com/centic9/commons-dost/status.svg?branch=master)](https://gradleupdate.appspot.com/centic9/commons-dost/status)
[![Release](https://img.shields.io/github/release/centic9/commons-dost.svg)](https://github.com/centic9/commons-dost/releases)
[![GitHub release](https://img.shields.io/github/release/centic9/commons-dost.svg?label=changelog)](https://github.com/centic9/commons-dost/releases/latest)
[![Tag](https://img.shields.io/github/tag/centic9/commons-dost.svg)](https://github.com/centic9/commons-dost/tags)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.dstadler/commons-dost/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.dstadler/commons-dost) 
[![Maven Central](https://img.shields.io/maven-central/v/org.dstadler/commons-dost.svg)](https://maven-badges.herokuapp.com/maven-central/org.dstadler/commons-dost)

This is a small library of code-pieces that I find useful in many of my projects. 

It covers areas that I miss in the JDK itself and also in Apache Commons libraries, e.g. for arrays and collections, logging, networking stuff, 
as well as helpers for testing.

## Contents

Here an (incomplete) list of bits and pieces in this lib:
* MappedCounter - counting things easily
* DateParser - print out things like "1h 2min ago" instead of "at 2015-04-01 16:23:23"
* ExecutionHelper - More easily run external processes and collect their output using Apache commons-exec underneath
* DeleteOnCloseInputStream - Delete a file as soon as the stream is closed
* SVNLogFileParser - Parse the XML output of 'svn log --xml ...'
* ZipFileCloseInputStream - Close a ZipFile as soon as the related InputStream (usually returned by the ZipFile) is closed
* ZipUtils - utilities for accessing content in Zip files recursively, i.e. access a file in a zip file inside a zip file.
* ThreadDump - get a printable output of all stack-traces similar to the output of the `jstack` tool
* ObjectAccessorList - a list which wraps another list and allows to transparently access single properties of the 
type of object contained in the original list.
* UnsupportedCollection and UnsupportedList as base implementations of Collection and List where all methods throw an exception
* DocumentStarter - knows how to open documents of known file-types on different operating systems, usually by using platform-specific tools
* ChromeDriverUtils - Get current installed version of Chrome and download a matching Selenium "chromedriver" executable
* ExecutorUtil - Helpers when working with Java Executors
* XMLHelper - Methods to create safe XML Parsers with dangerous features turned off
* GPXTrackpointsParser - Simple parser for GPX files

Please note that some testing related pieces were moved into their own library to better separate these things. Look at
http://github.com/centic9/commons-test for more details.

## Use it

### Gradle

    compile 'org.dstadler:commons-dost:1.+'

## Change it

### Grab it

    git clone https://github.com/centic9/commons-dost.git

### Build it and run tests

    cd commons-dost
    ./gradlew check jacocoTestReport

### Release it

* Check the version defined in `gradle.properties`
* Push changes to GitHub
* Publish the binaries to Maven Central

    ./gradlew --console=plain publishToMavenCentral

* This should automatically release the new version on MavenCentral
* Apply tag in Github
* Increase the version in `gradle.properties` afterwards
* Afterwards go to the [Github releases page](https://github.com/centic9/commons-dost/releases) and add release-notes

## Support this project

If you find this library useful and would like to support it, you can [Sponsor the author](https://github.com/sponsors/centic9)

## Licensing

* commons-dost is licensed under the [BSD 2-Clause License].
* A few pieces are imported from other sources, the source-files contain the necessary license pieces/references.

[BSD 2-Clause License]: https://www.opensource.org/licenses/bsd-license.php
