#!/bin/sh
git pull
mvn clean install -U -Dmaven.test.skip=true
mvn package -Dmaven.test.skip=true

if [ ! -d javaExtensions ];then
  mkdir -p javaExtensions
fi
if [ ! -d jars ];then
  mkdir -p jars
fi
if [ ! -d configs ];then
  mkdir -p configs
fi

rm -rf javaExtensions/*
cp -r target/classes/* javaExtensions
rm -rf jars/*
cp -r target/lib/* jars
cp target/classes/configs/common_conf/config.xml configs/common_conf/config.xml
cp -r target/classes/configs/* configs