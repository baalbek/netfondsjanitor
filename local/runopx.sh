#!/bin/bash

nohup clj netfondsjanitor/app.clj -O -x dlstockoptions.xml &

exit 0
