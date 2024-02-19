FROM openjdk:8-jre-alpine

#WORKDIR /home

#ENV componentName "TrustManager"
#ENV componentVersion 3.0.4

#RUN apk --no-cache add \
#	git \
#	unzip \
#	wget \
#	bash \
 #   && echo "Downloading $componentName $componentVersion" \
	#&& wget "https://jitpack.io/com/github/symbiote-h2020/$componentName/$componentVersion/$componentName-$componentVersion-run.jar"

#EXPOSE 8204

#CMD java $JAVA_HTTP_PROXY $JAVA_HTTPS_PROXY $JAVA_NON_PROXY_HOSTS -DSPRING_BOOT_WAIT_FOR_SERVICES=symbiote-aam:8080 -jar $(ls *run.jar)

ADD build/libs/TrustManager-3.1.0-SNAPSHOT-run.jar tm.jar
ENV JAVA_OPTS="-Xms512m -Xmx512m"
ENV APP_PROPS="--external.config=/src/main/resources/bootstrap.properties"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /tm.jar $APP_PROPS" ]