#!/bin/bash
set -e
mvn clean assembly:assembly -P linux-x86
mvn clean assembly:assembly -P linux-x86_64
mvn clean assembly:assembly -P windows-x86
mvn clean assembly:assembly -P windows-x86_64
