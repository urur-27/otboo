# 빌드 스테이지
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace/app

COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .

# Gradle wrapper 실행권한 부여 (UNIX 계열에서 필요)
RUN chmod +x gradlew

# 의존성 캐시
RUN ./gradlew dependencies

# 소스코드 복사 및 빌드
COPY src src
RUN ./gradlew build -x test

# 런타임 스테이지
FROM eclipse-temurin:21-jre-jammy
VOLUME /tmp

COPY --from=build /workspace/app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
