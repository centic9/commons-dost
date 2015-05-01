[![Build Status](https://buildhive.cloudbees.com/job/centic9/job/commons-dost/badge/icon)](https://buildhive.cloudbees.com/job/centic9/job/commons-dost/) 

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.dstadler/commons-dost/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.dstadler/commons-dost) [![Maven Central](https://img.shields.io/maven-central/v/org.dstadler/commons-dost.svg)](https://maven-badges.herokuapp.com/maven-central/org.dstadler/commons-dost)

This is a small library of code-pieces that I find useful in many of my projects. 

It covers areas that I miss in the JDK itself e.g. for arrays and collections, logging, networking stuff, 
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

Please note that some testing related pieces were moved into their own library to better separate these things. Look at
http://github.com/centic9/commons-test for more details.

## Use it

### Gradle

    compile 'org.dstadler:commons-dost:1.+'

## Change it

### Grab it

    git clone git://github.com/centic9/commons-dost

### Create Eclipse project files

	./gradlew eclipse

### Build it and run tests

	cd commons-dost
	./gradlew check jacocoTestReport

#### Licensing
* commons-dost is licensed under the [BSD 2-Clause License].
* A few pieces are imported from other sources, the source-files contain the necessary license pieces/references.

[BSD 2-Clause License]: http://www.opensource.org/licenses/bsd-license.php
