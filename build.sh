#!/bin/bash
mvn clean package spring-boot:repackage -Pprod
cp target/IDEonline-*.jar release/