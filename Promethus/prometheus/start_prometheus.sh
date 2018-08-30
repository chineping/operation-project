#!/bin/bash
nohup /opt/prometheus-2.3.2/prometheus --config.file="/opt/prometheus-2.3.2/prometheus.yml" --storage.tsdb.path="/opt/prometheus-2.3.2/data/prometheus" --storage.tsdb.retention=15d --web.enable-lifecycle  &
