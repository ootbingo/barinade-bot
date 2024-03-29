FROM eclipse-temurin:21

COPY build/libs/barinade_bot.jar /usr/bin

RUN mkdir /etc/barinade
VOLUME ["/etc/barinade"]
WORKDIR /etc/barinade

CMD ["java", "-jar", "/usr/bin/barinade_bot.jar"]
