#!/bin/bash
javac="javac -Xlint:unchecked"

$javac -cp src src/RmiServer.java
$javac -cp src src/RmiClient.java

if [ $1 = "client" ]; then
   java -cp src RmiClient
else
  java -cp src RmiServer
fi