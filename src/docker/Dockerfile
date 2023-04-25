FROM debian:11

MAINTAINER Dirk Stenger <stenger@lat-lon.de>
MAINTAINER Torsten Friebe <friebe@lat-lon.de>

RUN apt update && apt install -y openjdk-17-jre wget unzip
RUN wget https://archive.apache.org/dist/tomcat/tomcat-7/v7.0.109/bin/apache-tomcat-7.0.109.tar.gz
RUN tar -xvf apache-tomcat-7.0.109.tar.gz
RUN mv apache-tomcat-7.0.109 /usr/local/tomcat

# add TEAM engine webapp
ADD maven/dependency/teamengine-web-*.war /root/
RUN cd /root/ && unzip -q teamengine-web-*.war -d /usr/local/tomcat/webapps/teamengine

# add common libs
ADD maven/dependency/teamengine-web-*-common-libs.zip /root/
RUN cd /root/ && unzip -q teamengine-web-*-common-libs.zip -d /usr/local/tomcat/lib

# add TEAM engine console
ADD maven/dependency/teamengine-console-*-base.zip /root/
RUN cd /root/ && unzip -q teamengine-console-*-base.zip -d /root/te_base

# set TE_BASE
ENV JAVA_OPTS="-Xms1024m -Xmx2048m -DTE_BASE=/root/te_base"

# add ETS for OGC APP Features 1.0
ADD maven/ets-ogcapi-features10-*-ctl.zip /root/
RUN cd /root/ && unzip -q ets-ogcapi-features10-*-ctl.zip -d /root/te_base/scripts
ADD maven/ets-ogcapi-features10-*-deps.zip /root/
RUN cd /root/ && unzip -q -o ets-ogcapi-features10-*-deps.zip -d /usr/local/tomcat/webapps/teamengine/WEB-INF/lib

# run tomcat
CMD ["/usr/local/tomcat/bin/catalina.sh", "run"]