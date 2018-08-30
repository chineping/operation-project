#!/bin/bash
nohup /usr/share/logstash/bin/logstash -f /usr/share/logstash/bin/all.conf.dingtalk.conf >> nohup.out 2>&1 &
