#!/bin/bash

eth=$(ifconfig -s | grep -o -m 1 ^eth[0-9])
enp0s=$(ifconfig -s | grep -o -m 1 ^enp0s[0-9])


if [[ !  -z  $eth  ]]
then
  ifname=$eth
elif [[ !  -z  $enp0s  ]]
then
  ifname=$enp0s
else
  echo ERROR: Failed to identify network interface
  exit 0 #Exits with success so that parent build script can go on
fi

echo Configuring IP address on interface $ifname

sudo ip addr add 192.168.137.110/24 dev $ifname
sudo ip addr add 192.168.137.150/24 dev $ifname
