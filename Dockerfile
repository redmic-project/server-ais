FROM redmic/redmic-server

COPY /dist/*.jar ./

EXPOSE ${MICROSERVICE_PORT}

ENTRYPOINT java ${JAVA_OPTS} \
	-Djava.security.egd=file:/dev/./urandom \
	-jar ${DIRPATH}/${MICROSERVICE_NAME}.jar
