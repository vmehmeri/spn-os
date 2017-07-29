#!/bin/bash

DOCKER_BUILD_ARG=$1

CURRENT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASE_DIR=$CURRENT_DIR/../..
MVN_TARGET_DIR=$BASE_DIR/target

cd $BASE_DIR
mvn clean package -DskipTests

cp $MVN_TARGET_DIR/Arbiter.jar $CURRENT_DIR/arbiter
cp $MVN_TARGET_DIR/BigSwitchTenant.jar $CURRENT_DIR/tenant/Tenant.jar

echo "Configuring IP addresses..."
cd $CURRENT_DIR/scripts
sudo ./config_ip_addresses.sh

echo "Building docker container for tenant..."
cd $CURRENT_DIR/tenant
sudo docker build -t tenant . $1


