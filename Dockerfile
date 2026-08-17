FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ENV TZ=Asia/Shanghai
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"

COPY sellersprite-server/target/sellersprite-server-0.2.0.jar /app/sellersprite-server-0.2.0.jar

EXPOSE 8092

ENTRYPOINT ["java", "-jar", "/app/sellersprite-server-0.2.0.jar"]
