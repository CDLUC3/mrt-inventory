#*********************************************************************
#   Copyright 2021 Regents of the University of California
#   All rights reserved
#*********************************************************************

ARG ECR_REGISTRY=ecr_registry_not_set

FROM ${ECR_REGISTRY}/merritt-tomcat:dev
ARG COMMITDATE=''

COPY inv-war/target/mrt-invwar-*.war /usr/local/tomcat/webapps/inventory.war

RUN mkdir -p /build/static && \
    echo "mrt-inventory: ${COMMITDATE}" > /build/static/build.content.txt && \
    jar uf /usr/local/tomcat/webapps/inventory.war -C /build static/build.content.txt

RUN mkdir -p /apps/replic/tst/inv/log /tdr/tmpdir
