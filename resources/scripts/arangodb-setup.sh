#!/usr/bin/env bash
set -eux

# Install ArangoDB
wget https://download.arangodb.com/arangodb37/Community/linux/arangodb3-3.7.20-centos.x86_64.rpm
rpm --install arangodb3-3.7.20-centos.x86_64.rpm

systemctl enable arangodb
systemctl start  arangodb
sleep 10

# Create database & collection
arangosh --server.endpoint tcp://127.0.0.1:8529 \
  --javascript.execute-string \
"db._createDatabase('jepsen'); \
 db._useDatabase('jepsen'); \
 db._createCollection('kv', { replicationFactor: 3, writeConcern: 3 });"
