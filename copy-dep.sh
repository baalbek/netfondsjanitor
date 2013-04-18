#!/bin/bash

# mvn dependency:copy-dependencies -DoutputDirectory=./src/main/clojure/

mvn dependency:copy-dependencies -DoutputDirectory=./target/


cd target

ln -s /usr/local/share/java/jline-0.9.94.jar
#ln -s /home/rcs/.m2/repository/org/clojure/clojure/1.4.0/clojure-1.4.0.jar
#ln -s /home/rcs/.m2/repository/org/clojure/clojure-contrib/1.2.0/clojure-contrib-1.2.0.jar

cd ../

exit 0
