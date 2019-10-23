#! /bin/bash

if (( $# < 2 ))
  then
    echo "usage: ./run_newman.sh <num_times> <num_rows>"
    exit 1
fi

for i in `seq 0 $1`
do
  eval 'newman run hmda-filing-api-test-'$2'.json -d hmda-filing-2018-config-bank0-'$2'.json'
done
