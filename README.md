[![Build Status](https://buildhive.cloudbees.com/job/centic9/job/commons-dost/badge/icon)](https://buildhive.cloudbees.com/job/centic9/job/commons-dost/) 

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.dstadler/commons-dost/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.dstadler/commons-dost)

This is a small library of code-pieces that I find useful in many of my projects. 

It covers areas like things that I miss in the JDK itself e.g. for arrays and collections, logging, networking stuff, 
as well as helpers for testing.

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
