#!/bin/bash

# Install Erlang

sudo apt-get install -y gcc wget make autoconf openssl libssl-dev libncurses5 libncurses5-dev

cd
wget http://erlang.org/download/otp_src_R16B.tar.gz
tar zxf otp_src_R16B.tar.gz
cd otp_src_R16B
sudo ./configure
sudo make
sudo make install

# Install LINC-OE
sudo apt-get install -y bridge-utils libpcap0.8 libpcap-dev libcap2-bin uml-utilities

cd
git clone https://github.com/vmehmeri/linc-oe
cd linc-oe
# build
sudo make rel

# Install linc config generator
cd
git clone https://github.com/FlowForwarding/LINC-config-generator.git
cd LINC-config-generator
# build
make
