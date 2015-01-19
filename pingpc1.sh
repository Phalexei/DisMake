#! /bin/bash

ping ensipc"$1" -w1 -q > /dev/null 2>&1
if [ $? == 0 ]
then
echo "ensipc""$1"
fi

