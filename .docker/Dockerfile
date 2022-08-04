# Base image
FROM radius17/openjdk-11:latest
#####################################################################################
RUN keytool -noprompt -genkeypair -alias springboot -keyalg RSA -keysize 4096 \
    -storetype PKCS12 -keystore springboot.p12 -validity 3650  -storepass 1234567 \
    -dname CN=LocalDevelopment,OU=,O=Developer,L=,ST=,C=RU
RUN keytool -list -storetype pkcs12 -keystore springboot.p12 -storepass 1234567
#####################################################################################
ENV TZ=Europe/Moscow