#!/bin/bash

JANITOR_HOME=/home/rcs/opt/java/netfondsjanitory/dist
JAVA_HOME=/usr/local/java

export PATH=$JANITOR_HOME:$JAVA_HOME/bin:$PATH

java -jar /home/rcs/opt/java/netfondsjanitor/netfondsjanitor-3.4.jar -O -S -x dlstockoptions.xml

# java -jar /home/rcs/opt/java/netfondsjanitor/dist/netfondsjanitor-3.3.jar -O -x dlstockoptions.xml

java -jar /home/rcs/opt/java/netfondsjanitor/netfondsjanitor-3.4.jar -p -f -t OSEBX

# java -jar /home/rcs/opt/java/netfondsjanitor/netfondsjanitor-3.4.jar -p -f -t OBX

exit 0
