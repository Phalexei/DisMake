#! /bin/bash

COUNT=1
while  [ $COUNT -le 9 ]
do
./pingpc1.sh 0"$COUNT" &
./pingpc2.sh 0"$COUNT" &
./pingpc3.sh 0"$COUNT" &
COUNT=$(($COUNT + 1)) 
done

while  [ $COUNT -le 100 ]
do
./pingpc1.sh "$COUNT" &
./pingpc2.sh "$COUNT" &
./pingpc3.sh "$COUNT" &
COUNT=$(($COUNT + 1)) 
done

sleep 2

while  [ $COUNT -le 200 ]
do
./pingpc1.sh "$COUNT" &
./pingpc2.sh "$COUNT" &
./pingpc3.sh "$COUNT" &
COUNT=$(($COUNT + 1)) 
done

sleep 2

while  [ $COUNT -le 300 ]
do
./pingpc1.sh "$COUNT" &
./pingpc2.sh "$COUNT" &
./pingpc3.sh "$COUNT" &
COUNT=$(($COUNT + 1)) 
done

wait