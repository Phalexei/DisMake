#!/bin/sh -xe

#./detectpc.sh 2> /dev/null | sed $2'q' > clients.txt

HOST=`hostname`
mvn

CURPATH=`pwd`
Z=$(readlink -f $1)
DIR=$(dirname $Z)
FILE=$(basename $1)
cd $DIR
echo `pwd`

java -jar $CURPATH/target/DisMake.jar --server $HOST $FILE &

pid=$!
for line in $(cat $CURPATH/clients.txt); 
do 
#echo "$line" ;
#scp -r $DIR $line:/$(dirname $DIR)
ssh -o "StrictHostKeyChecking no" $line "
  cd $DIR
java -jar $CURPATH/target/DisMake.jar --client $HOST $3
" &
done
