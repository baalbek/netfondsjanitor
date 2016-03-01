#!/bin/bash

# sudo -u trader ./dump.py -F -u trader -d trader -f trader.dump

sudo -u trader pg_dump -U trader | gzip > /home/rcs/database/dumps/trader-2016.2.21.dump.gz

exit 0
