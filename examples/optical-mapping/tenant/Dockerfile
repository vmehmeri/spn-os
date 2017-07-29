FROM ubuntu:14.04
MAINTAINER Victor Mehmeri

RUN apt-get update 
RUN apt-get install software-properties-common debconf-utils -y
RUN add-apt-repository ppa:webupd8team/java -y
RUN apt-get update
RUN echo "oracle-java-8-installer shared/accepted-oracle-license-v1-1 select true" | debconf-set-selections
RUN apt-get install oracle-java8-installer oracle-java8-set-default -y
RUN apt-get install vim python3 python3-pip -y
RUN pip3 install flask 
COPY . /root
WORKDIR /root

