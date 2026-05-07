#!/bin/bash
set -e

# Ensure directories exist and are writable
mkdir -p /data/node7000 /data/node7001 /data/node7002

start_node() {
  local port=$1
  valkey-server \
    --port $port \
    --cluster-enabled yes \
    --cluster-config-file nodes-${port}.conf \
    --cluster-announce-ip 127.0.0.1 \
    --cluster-node-timeout 5000 \
    --appendonly yes \
    --dir /data/node${port} \
    --bind 0.0.0.0 \
    --protected-mode no \
    --daemonize yes \
    --logfile /data/node${port}/valkey.log
}

echo "Starting Valkey nodes..."
start_node 7000
start_node 7001
start_node 7002

# Wait for nodes to respond
until valkey-cli -p 7000 ping | grep -q PONG; do
  echo "Waiting for node 7000..."
  sleep 1
done

echo "Creating cluster..."
valkey-cli --cluster create 127.0.0.1:7000 127.0.0.1:7001 127.0.0.1:7002 --cluster-yes

# Wait for full slot assignment
echo "Waiting for cluster slots to settle..."
until [ "$(valkey-cli -p 7000 cluster info | grep cluster_slots_ok | cut -d: -f2 | tr -d '\r')" -eq 16384 ]; do
  sleep 1
done

# The magic string for Testcontainers
echo "Valkey cluster is ready!"

# Tail the log file instead of the AOF file
tail -f /data/node7000/valkey.log