FROM openjdk:8-jre-alpine

MAINTAINER Ashwin Shenoy "Ashwin.Shenoy@thomsonreuters.com"

WORKDIR /app/RESTelasticsearch/

ENV JAVA_VERSION 8u31
ENV PACKAGE_ROOT /app

EXPOSE 8016

ADD app /app/

ENTRYPOINT ["/app/RESTelasticsearch/bin/start.sh"]

CMD []
