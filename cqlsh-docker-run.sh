#!/usr/bin/env bash
until printf "" 2>>/dev/null >>/dev/tcp/cassandra/9042; do
    sleep 5;
    echo "Waiting for cassandra...";
done

echo "Creating keyspaces and tables"
/usr/bin/cqlsh cassandra -f /tmp/dev-cassandra.cql