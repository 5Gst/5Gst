#!/bin/bash
set -e

if [ -z "$1" ]
then
  echo "Expected server host as an argument"
  exit 1
fi

export SERVER_HOST="$1"

echo "## Load Docker images from *.tar"
docker load -i service.tar.gz
docker load -i balancer.tar.gz
docker load -i postgres.tar.gz

echo "## Server host: " SERVER_HOST

echo "## Start Docker Compose"
docker compose up -d
