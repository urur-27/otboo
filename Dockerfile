# ===== Build stage =====
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace/app

# gradle 파일 먼저 복사 → 캐시 활용
COPY gradlew ./
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

# 의존성만 먼저 받기 (소스 없이) → 캐시 레이어 생성
RUN ./gradlew dependencies --no-daemon || true

# 소스 복사 후 빌드
COPY src src
RUN ./gradlew clean bootJar -x test --no-daemon

# ===== Runtime stage =====
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /workspace/app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]