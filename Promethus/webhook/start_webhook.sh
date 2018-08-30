#!/bin/bash
nohup go run /usr/local/go/src/github.com/yunlzheng/alertmanaer-dingtalk-webhook/cmd/webhook/webhook.go -defaultRobot=https://oapi.dingtalk.com/robot/send?access_token=xx &
