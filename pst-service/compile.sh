#!/bin/bash

docker run -v $(PWD):/usr/src/app -w /usr/src/app maven:3.5-jdk-8-alpine mvn package
