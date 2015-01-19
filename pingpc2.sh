#! /bin/bash

ping ensiarchi"$1" -w1 -q > /dev/null 2>&1
if [ $? == 0 ]
then
echo "ensiarchi""$1"
fi
