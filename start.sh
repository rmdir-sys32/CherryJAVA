#!/usr/bin/env bash 

### Ensure the script exits if a command fails

set -e  

echo "🔌 Connecting to your local Redis clone via Docker CLI..." 

### Run the temporary Docker container pointing to the host machine

docker run -it --rm redis redis-cli -h host.docker.internal -p 6379