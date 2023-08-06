#!/bin/bash
set -e

echo "## Loading Docker images from *.tar"
docker load -i service.tar.gz
docker load -i balancer.tar.gz
docker load -i postgres.tar.gz
