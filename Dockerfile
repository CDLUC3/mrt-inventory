#*********************************************************************
#   Copyright 2021 Regents of the University of California
#   All rights reserved
#*********************************************************************

ARG ECR_REGISTRY=ecr_registry_not_set

FROM ${ECR_REGISTRY}/merritt-tomcat:dev

COPY inv-war/target/mrt-invwar-*.war /usr/local/tomcat/webapps/inventory.war

RUN mkdir -p /apps/replic/tst/inv/log /tdr/tmpdir
