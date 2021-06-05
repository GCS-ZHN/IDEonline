#!/bin/bash
mvn clean package spring-boot:repackage -Pdev
rm -rf dev/IDEonline-*.jar
cp target/IDEonline-*.jar dev/