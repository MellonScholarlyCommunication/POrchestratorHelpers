#!/bin/bash

environment="prod"
names=(alice bob registration)
if [ -f DEVELOPMENT ]; then
    environment="dev"
fi

echo "[${environment}]"
for name in "${names[@]}"; do
    output=$(jq -r .output etc/${environment}/${name}.json)
    inputs=($(jq -r '.input | @sh' etc/${environment}/${name}.json | tr -d \'\")) 
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