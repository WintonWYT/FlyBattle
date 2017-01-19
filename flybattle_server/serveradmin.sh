#!/bin/sh
ulimit -n 65536
if [ $1 == "start" ]
then
  localdir=/home/logs/server/gc
  today=`date +%Y-%m-%d`
  if [ ! -d $localdir ]
  then
    mkdir -p $localdir
  fi
  nohup java -server -Xms1024m -Xmx1024m -Xmn512m -XX:MaxTenuringThreshold=3 -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:ParallelGCThreads=2 -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCApplicationStoppedTime -XX:-OmitStackTraceInFastThrow -XX:+PrintTenuringDistribution \
  -Xloggc:$localdir/gc_$today.log \
  -cp "./javaExtensions/:./jars/*" \
  -Dfile.encoding=UTF-8 starter.ServerStarter $2 $3 $4 >/dev/null 2>/dev/null &
exit 0
else
  if [ $1 == "console" ]
  then
    java -server -Xms512m -Xmx512m -Xmn256m -XX:MaxTenuringThreshold=3 -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:ParallelGCThreads=2 -XX:-OmitStackTraceInFastThrow \
    -cp "./javaExtensions/:./jars/*" \
    -Dfile.encoding=UTF-8 starter.ServerStarter $2 $3 $4
    exit 0
  else
    if [ $1 == "stop" ]
    then
      java -cp "./admin/*" \
      com.baitian.msadmin.AdminStarter altraserver-admin altraserver-whoami! $2 0 aes
    else
      if [ $1 == "reloader" ]
      then
        java -cp "./admin/*" \
        com.baitian.msadmin.AdminStarter altraserver-admin altraserver-whoami! $2 100 aes $3 $4
      fi
    fi
  fi
fi
exit 0