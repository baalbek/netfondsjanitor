#!/bin/bash

JANITOR_HOME=/home/rcs/opt/java/netfondsjanitory/dist
export JANITOR_HOME

JAVA_HOME=/usr/local/java
export JAVA_HOME

export PATH=$JANITOR_HOME:$JAVA_HOME/bin:$PATH

java -jar /home/rcs/opt/java/netfondsjanitor/dist/netfondsjanitor-3.2.jar -R -x dlstockoptions.xml

exit 0
