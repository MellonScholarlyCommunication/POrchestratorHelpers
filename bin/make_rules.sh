#!/bin/bash

environment="prod"
names=(alice bob registrator archivator)
if [ -f DEVELOPMENT ]; then
    environment="dev"
fi

echo "[${environment}]"
for name in "${names[@]}"; do

    if [ ! -e etc/${environment}/${name}.json ]; then
      continue
    fi
    
    output=$(jq -r .output etc/${environment}/${name}.json)
    inputs=($(jq -r '.input | @sh' etc/${environment}/${name}.json | tr -d \'\")) 

    # clean the old rules
    rm ${output}/*.n3

    # make the new rules
    for input in "${inputs[@]}"; do
      if compgen -G "${input}/*.n3" > /dev/null; then
        for f in ${input}/*.n3 ; do
            base=$(basename $f)

        	echo "${f} > ${output}/${base}" 
            npm exec mustache etc/${environment}/${name}.json ${f} > ${output}/${base}
        done
      fi
    done
done