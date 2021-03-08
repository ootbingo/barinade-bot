#!/bin/bash

echo "$2" | docker login docker.pkg.github.com -u $1 --password-stdin

case $(git branch --show-current) in
	master) TAG=prod ;;
	develop) TAG=int ;;
	*) echo "Building images is only available on master and develop" && exit 1 ;;
esac

IMAGE_NAME=bot
IMAGE_ID=docker.pkg.github.com/ootbingo/barinade-bot/$IMAGE_NAME

docker build . --tag $IMAGE_NAME
docker tag $IMAGE_NAME $IMAGE_ID:$TAG
docker push $IMAGE_ID:$TAG
