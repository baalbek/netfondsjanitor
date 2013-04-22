#!/bin/bash

# JAVA

JAVA_HOME=/usr/local/java
export JAVA_HOME

export PATH=$JAVA_HOME/bin:$PATH


cd /home/rcs/opt/java/netfondsjanitor/target

/usr/local/bin/clj netfondsjanitor/app.clj


exit 0


