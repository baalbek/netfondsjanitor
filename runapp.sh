#!/bin/bash

# JAVA
JAVA_HOME=/usr/local/java
export JAVA_HOME

export PATH=$JAVA_HOME/bin:$PATH

cd /home/rcs/opt/java/netfondsjanitor/target

CP=.:src:classes:test-classes

for f in *.jar
do
    CP=$CP:$f
done

# /usr/local/bin/clj netfondsjanitor/app.clj

exec java -cp $CP clojure.main netfondsjanitor/app.clj

exit 0


