#!/bin/bash

for file in $(ls yaml/) 
do 
	sudo docker cp yaml/$file tenant:/root/vno1/yaml/
done
