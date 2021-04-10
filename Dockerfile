FROM adoptopenjdk:11-jdk-openj9-bionic
MAINTAINER Ian.Ibbotson@k-int.com
VOLUME /tmp
COPY service/build/libs/mod-ciim-*.*.*.jar mod-ciim.war
EXPOSE 8080/tcp
# See https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config
#     https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config-relaxed-binding-from-environment-variables
CMD java -Djava.security.egd=file:/dev/./urandom -Xshareclasses -Xscmx50M -Xtune:virtualized -jar /mod-ciim.war
