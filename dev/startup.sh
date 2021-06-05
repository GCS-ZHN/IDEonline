#!/usr/bin/bash
ROOT=`dirname $0`
cd $ROOT
nohup java -jar IDEonline-*.jar $@ 2>&1 1>spring.log &
