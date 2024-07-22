#*********************************************************************
#   Copyright 2021 Regents of the University of California
#   All rights reserved
#*********************************************************************

ARG ECR_REGISTRY=ecr_registry_not_set

FROM ${ECR_REGISTRY}/merritt-tomcat:dev

COPY inv-war/target/mrt-invwar-*.war /usr/local/tomcat/inventory/inventory.war

RUN mkdir -p /build/static
RUN date -r /usr/local/tomcat/webapps/inventory.war +'mrt-ingest: %Y-%m-%d:%H:%M:%S' > /build/static/build.content.txt 
RUN jar uf /usr/local/tomcat/webapps/inventory.war -C /build static/build.content.txt

RUN mkdir -p /apps/replic/tst/inv/log /tdr/tmpdir
