#!/bin/bash

DOCKER_BUILD_ARG=$1


DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASE_DIR=$DIR/../..
LINC_OE_DIR=~/linc-oe
TOSCA_PARSER_DIR=$BASE_DIR/tools/tosca-nfv-parser
MVN_TARGET_DIR=$BASE_DIR/target

cd $BASE_DIR
mvn clean package -DskipTests

cp $DIR/network/sys.config $LINC_OE_DIR/rel/linc/releases/1.0/sys.config
cp $MVN_TARGET_DIR/Arbiter.jar $DIR/arbiter
cp $MVN_TARGET_DIR/OpticalMappingTenant.jar $DIR/tenant/Tenant.jar
cp -r $TOSCA_PARSER_DIR $DIR/tenant/

echo "Configuring IP addresses..."
cd $DIR/scripts
sudo ./config_ip_addresses.sh

echo "Building docker containers for tenants..."
cd $DIR/tenant	
sudo docker build -t opticaltenant . $1

