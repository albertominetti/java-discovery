#! /bin/bash
set -e
set -x

if [[ $1 == '' ]]; then
  echo "Please use the commands: start, stop or status" >&2
fi

if [[ $1 == 'start' ]]; then
  mvn clean package -DskipTests
  docker-compose -f docker-compose-infra.yml -f docker-compose-provider.yml -f docker-compose-app.yml up --build -d --scale provider=3
elif [[ $1 == 'stop' ]]; then
  docker-compose -f docker-compose-infra.yml -f docker-compose-provider.yml -f docker-compose-app.yml down -v
elif [[ $1 == 'status' ]]; then
  docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
fi

echo "Done" >&2