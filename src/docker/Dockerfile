FROM tomcat:10.1-jre17

MAINTAINER Dirk Stenger <stenger@lat-lon.de>
MAINTAINER Torsten Friebe <friebe@lat-lon.de>

RUN apt update && apt install -y unzip

# add TEAM engine webapp
ADD maven/dependency/teamengine-web-*.war /root/
RUN cd /root/ && unzip -q teamengine-web-*.war -d /usr/local/tomcat/webapps/teamengine

# add common libs
ADD maven/dependency/teamengine-web-*-common-libs.zip /root/
RUN cd /root/ && unzip -q teamengine-web-*-common-libs.zip -d /usr/local/tomcat/lib

# add TEAM engine console
ADD maven/dependency/teamengine-console-*-base.zip /root/
RUN cd /root/ && unzip -q teamengine-console-*-base.zip -d /root/te_base

# add ETS for OGC API Features 1.0
ADD maven/ets-ogcapi-features10-*-ctl.zip /root/
RUN cd /root/ && unzip -q ets-ogcapi-features10-*-ctl.zip -d /root/te_base/scripts
ADD maven/ets-ogcapi-features10-*-deps.zip /root/
RUN cd /root/ && unzip -q -o ets-ogcapi-features10-*-deps.zip -d /usr/local/tomcat/webapps/teamengine/WEB-INF/lib

# set TE_BASE
ENV JAVA_OPTS="-Xms1024m -Xmx2048m -DTE_BASE=/root/te_base"

RUN rm -R /root/te_base/scripts/note

# run tomcat
CMD ["catalina.sh", "jpda", "run"]
