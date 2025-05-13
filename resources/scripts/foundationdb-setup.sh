#!/usr/bin/env bash
set -eux

# Install FoundationDB
yum install -y https://repos.foundationdb.org/fdb/releases/7.1.19/redhat7/foundationdb-clients-7.1.19-1.x86_64.rpm \
               https://repos.foundationdb.org/fdb/releases/7.1.19/redhat7/foundationdb-server-7.1.19-1.x86_64.rpm

# Example 5-node cluster file (adjust addresses as needed)
cat <<EOF >/etc/foundationdb/fdb.cluster
cluster fdb-cluster-00
token your-very-long-token-string
127.0.0.1:4500
127.0.0.1:4501
127.0.0.1:4502
127.0.0.1:4503
127.0.0.1:4504
EOF

systemctl enable foundationdb
systemctl start  foundationdb
