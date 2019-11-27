#!/bin/sh

export LIBS=/app/RESTelasticsearch/libs
export LOGBACKCFG="-Dlogging.config=/app/RESTelasticsearch/config/logback.xml"
java ${LOGBACKCFG} -jar ${LIBS}/restelasticsearch-1.0-SNAPSHOT.jar

