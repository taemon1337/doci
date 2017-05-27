#!/bin/bash

docker run -v $(pwd)/.m2:/root/.m2 -v $(pwd):/usr/src/app -w /usr/src/app maven:3.5-jdk-8-alpine mvn package
