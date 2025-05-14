#!/usr/bin/env bash
set -eux

# Install Couchbase Server
wget https://packages.couchbase.com/releases/7.2.2/couchbase-server-enterprise-7.2.2-centos7.x86_64.rpm
rpm --install couchbase-server-enterprise-7.2.2-centos7.x86_64.rpm
sleep 10

# Initialize cluster
couchbase-cli cluster-init \
  --cluster-username Admin \
  --cluster-password password \
  --services data,index,query \
  --cluster-ramsize 2048

# Create bucket "jepsen" with 2 replicas
couchbase-cli bucket-create \
  --cluster localhost \
  --username Admin \
  --password password \
  --bucket jepsen \
  --bucket-type couchbase \
  --bucket-ramsize 1024 \
  --bucket-replica 2 \
  --enable-flush 0

# Enable majority durability
couchbase-cli bucket-edit \
  --cluster localhost \
  --username Admin \
  --password password \
  --bucket jepsen \
  --durability-level majority