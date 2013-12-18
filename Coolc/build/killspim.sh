#! /bin/bash

for i in `ps -ef | grep spim | awk '{ print $2 }'`; do kill -9 $i; done
