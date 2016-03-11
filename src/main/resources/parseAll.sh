#!/bin/bash

FILES=$(ls *.sentences)
for f in $FILES
do
    filename="${f%.*}"
    newname="${filename}.parse"
    echo "Processing $f"
    /Users/leah/Tulip/Code/turbo_parser/TurboParser-2.3.0/scripts/parse.sh $f > $newname
done
