#!/bin/bash
set -e

if [ -z "$1" ]
then
  echo "Expected server host as an argument"
  exit 1
fi

export SERVER_HOST="$1"
echo "## Using server host: '$SERVER_HOST'"

function loadSecretFromFile {
  if [ -z "$1" ]
  then
    echo "Expected file name as a first loadSecretFromFile function argument"
    exit 1
  fi

  if [[ ! -f "$1" ]]; then
    echo "## Generating secret key in file $1"
    openssl rand -hex 32 | cat > "$1"
  else
    echo "## Using already generated key from file $1"
  fi

  retval="$(cat $1)"
}

loadSecretFromFile .db.pass
export DB_PASS="$retval"

loadSecretFromFile .balancer.key
export BALANCER_SECRET_KEY="$retval"

loadSecretFromFile .service.key
export SERVICE_SECRET_KEY="$retval"

echo "## WARNING: Starting server without TLS"
export SERVICE_URL_SCHEME=http

echo "## Starting Docker Compose"
docker compose up -d
