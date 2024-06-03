#!/bin/bash

# file containing cache configurations to simulate
input_file="configurations.txt"

# tracefile is passed as the first and only parameter to the script
tracefile="$1"

# Read each line from the input file
while read -r line; do
    # Extract the numeric and tracefile name parameters
    n1=$(echo "$line" | awk '{print $1}')
    n2=$(echo "$line" | awk '{print $2}')
    n3=$(echo "$line" | awk '{print $3}')

    # Execute the cache simulation command with provided parameters
    java -jar simulator.jar -s "$n1" -E  "$n2" -b "$n3" -t "$tracefile"
done < "$input_file"


