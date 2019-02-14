#!/bin/bash

#NC_BIN=nc.traditional
#NC_BIN=nc
SLEEP_LENGTH=8

WAIT_HOSTS="${@}"

echo "Will wait for fallowing hosts getting started:"
echo "${WAIT_HOSTS}"

wait_for_service() {
  echo Waiting for $1 to listen on $2...
   LOOP=1
   while [ "$LOOP" != "0" ];
     do
     echo "Waiting on host: ${1}:${2}"
     sleep $SLEEP_LENGTH
     curl $1:$2
     RESULT=$?
     echo "Service check result: $RESULT"
     if [ $RESULT == 0  ]
     then
       LOOP=0
     fi
     done
}

echo
echo "Waiting for components to start"
echo

for URL in `echo "${WAIT_HOSTS}"`
do
  wait_for_service `echo $URL | sed s/\:/\ /`
  echo "Host $URL detected started."
done

echo
echo "All services detected as started."
echo

exit 0
