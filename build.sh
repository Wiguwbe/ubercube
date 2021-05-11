#!/bin/bash
docker run -it --rm --name my-maven-project -v "$(pwd)":/usr/src/mymaven -w /usr/src/mymaven maven:3-openjdk-8 mvn package assembly:single
