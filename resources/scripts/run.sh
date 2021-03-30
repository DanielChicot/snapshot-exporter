#!/bin/bash

aws s3 cp s3://danc-nifi-stub/snapshot-exporter/snapshot-exporter-0.0.1-SNAPSHOT-all.jar .

export HADOOP_CLASSPATH=./snapshot-exporter-0.0.1-SNAPSHOT-all.jar:$(find /usr/lib/hbase/ -type f -name '*.jar' | xargs | tr ' ' ':'):/etc/hbase/conf
hadoop jar ./snapshot-exporter-0.0.1-SNAPSHOT-all.jar automatedtests:dataworks_aws_ingest_consumers_development_176_1 danc-nifi-stub 5
