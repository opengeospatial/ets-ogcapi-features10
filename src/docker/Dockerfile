FROM tomcat:7.0-jre8 as builder

# add TEAM engine webapp
ADD maven/dependency/teamengine-web-*.war /root/
RUN cd /root/ && unzip -q teamengine-web-*.war -d /usr/local/tomcat/webapps/teamengine

# add common libs
ADD maven/dependency/teamengine-web-*-common-libs.zip /root/
RUN cd /root/ && unzip -q teamengine-web-*-common-libs.zip -d /usr/local/tomcat/lib

# add TEAM engine console
ADD maven/dependency/teamengine-console-*-base.zip /root/
RUN cd /root/ && unzip -q teamengine-console-*-base.zip -d /root/te_base


# add ETS for OGC APP Features 1.0
ADD maven/ets-ogcapi-features10-*-ctl.zip /root/
RUN cd /root/ && unzip -q ets-ogcapi-features10-*-ctl.zip -d /root/te_base/scripts
ADD maven/ets-ogcapi-features10-*-deps.zip /root/
RUN cd /root/ && unzip -q -o ets-ogcapi-features10-*-deps.zip -d /usr/local/tomcat/webapps/teamengine/WEB-INF/lib

FROM tomcat:7.0-jre8

MAINTAINER Dirk Stenger <stenger@lat-lon.de>
MAINTAINER Torsten Friebe <friebe@lat-lon.de>

# set TE_BASE
ENV JAVA_OPTS="-Xms1024m -Xmx2048m -DTE_BASE=/root/te_base"

COPY --from=builder /usr/local/tomcat/ /usr/local/tomcat/
COPY --from=builder /root/te_base /root/te_base

# run tomcat
CMD ["catalina.sh", "jpda", "run"]
