#!/bin/bash

set -e
mvn clean
mvn assembly:assembly -P linux
mvn assembly:assembly -P linux-64
mvn assembly:assembly -P windows
mvn assembly:assembly -P windows-64
mvn assembly:assembly -P macosx
mvn assembly:assembly -P macosx-cocoa
mvn assembly:assembly -P macosx-cocoa-64
[ ! -d $1 ] && exit 0
cp target/c10t-swt-linux-x86-jar-with-dependencies.jar $1
cp target/c10t-swt-linux-x86_64-jar-with-dependencies.jar $1
cp target/c10t-swt-windows-x86-jar-with-dependencies.jar $1
cp target/c10t-swt-windows-x86_64-jar-with-dependencies.jar $1
cp target/c10t-swt-macosx-cocoa-x86-jar-with-dependencies.jar $1
cp target/c10t-swt-macosx-cocoa-x86_64-jar-with-dependencies.jar $1
cp target/c10t-swt-macosx-carbon-x86-jar-with-dependencies.jar $1
