#!/bin/sh
curpwd=`pwd`
cd /d:/flybattle_server
./stop.sh
echo "shutdown server, start build..."
./compile.sh
echo "build success, startup server..."
./start.sh
cd $curpwd