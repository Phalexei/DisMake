#! /bin/bash

ping ensipsys"$1" -w1 -q > /dev/null 2>&1
if [ $? == 0 ]
then
echo "ensipsys""$1"
fi