#!/bin/bash


aws s3 cp s3://danc-nifi-stub/snapshot-exporter/snapshot-exporter-0.0.1-SNAPSHOT.jar .

export HADOOP_CLASSPATH=$(find /usr/lib/hbase/ -type f -name '*.jar' | xargs | tr ' ' ':'):/etc/hbase/conf:./snapshot-exporter-0.0.1-SNAPSHOT.jar
export MAP_REDUCE_OUTPUT_DIRECTORY=/user/hadoop/import/$(uuidgen)

hadoop jar ./snapshot-exporter-0.0.1-SNAPSHOT.jar $@
