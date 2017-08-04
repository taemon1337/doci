#!/bin/bash

image=bleve-explorer
version=latest

# Build docker image
docker build -t $image:$version .

# Compile app for linux
docker run --rm -v $(PWD):/output -e GOOS=linux -e GOARCH=amd64 $image go build -v -o /output/bin/$image-linux-amd64

# Compile app for darwin/mac
docker run --rm -v $(PWD):/output -e GOOS=darwin -e GOARCH=amd64 $image go build -v -o /output/bin/$image-darwin-amd64

# Cross compile for windows
docker run --rm -v $(PWD):/output -e GOOS=windows -e GOARCH=386 $image go build -v -o /output/bin/$image-windows-386

