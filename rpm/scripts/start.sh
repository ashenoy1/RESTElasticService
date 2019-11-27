#! /bin/bash

export LOGBACKCFG="-Dlogging.config=/opt/asgard/RESTElasticService/config/logback.xml"
export JAR=/opt/asgard/RESTElasticService/libs/restelasticsearch-1.0-RC1.jar
#export CFG=/opt/asgard/RESTElasticService/config/RESTElasticService.yml
export CFG=/opt/asgard/RESTElasticService
export OUT=/var/log/asgard/RESTelasticsearch/RESTelasticservice_console.log
${JAVA_HOME}/bin/java -enableassertions  ${LOGBACKCFG} -jar ${JAR} ${CFG}  >> ${OUT} 2>&1  &