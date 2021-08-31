FROM openjdk:11.0-jre-buster
WORKDIR /

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && dpkg-reconfigure -f noninteractive tzdata

RUN echo 'deb https://mirrors4.tuna.tsinghua.edu.cn/debian/ buster main contrib non-free' > /etc/apt/sources.list
RUN apt-get clean && apt-get update && apt-get install -y locales && localedef -c -f UTF-8 -i zh_CN zh_CN.utf8 && apt-get clean && rm -rf /var/lib/apt/lists/*
ENV LANG zh_CN.utf8

COPY testimg/ /testimg/
COPY build/libs/*.jar /app.jar
CMD ["java", "-jar", "app.jar"]