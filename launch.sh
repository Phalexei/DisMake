#!/bin/sh -xe

HOST=`hostname`
mvn

CURPATH=`pwd`
Z=$(readlink -f $1)
DIR=$(dirname $Z)
FILE=$(basename $1)
cd $DIR
echo `pwd`

for line in $(cat $CURPATH/clients.txt); do 
    ssh -o "StrictHostKeyChecking no" $line "mkdir -p $(dirname $DIR)"
    scp -r -q $DIR $line:$(dirname $DIR)
done

for line in $(cat $CURPATH/clients.txt); do
    ssh -o "StrictHostKeyChecking no" $line "
        cd $DIR
        java -jar $CURPATH/target/DisMake.jar --client $HOST $2
    " &
done

time -p java -jar $CURPATH/target/DisMake.jar --server $HOST $FILE $3

