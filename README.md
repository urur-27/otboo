# sb02-otboo-team03

## ☀️ 프로젝트 개요
날씨 기반 OOTD 추천 서비스, 옷장을 부탁해 (개인화 의상 및 아이템 추천 SaaS)

> 개발기간: 2025.07.28 ~ 2025.08.30

## 🔧 기술 스택

#### 🚀 백엔드

![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)

#### 💿 데이터베이스

![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white)

#### ☁️ 클라우드

![AWS](https://img.shields.io/badge/AWS-FF9900?style=for-the-badge&logo=amazon-aws&logoColor=white)

#### 🛠️ 도구 & CI/CD

![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white)

## 🔗 시연 영상
[SB02-team3 시연 영상](https://drive.google.com/file/d/16Rl1J8x3K6qMmDdAeg7EfeUL-W6oSGXP/view?usp=sharing)

## 시스템 아키텍처
#### 프론트엔드 정적 리소스 시스템 아키텍처
<img width="688" height="305" alt="image" src="https://github.com/user-attachments/assets/b663b01d-21ad-4f6d-9768-154452f43144" />


#### 전체 시스템 아키텍처
<img width="915" height="335" alt="image" src="https://github.com/user-attachments/assets/6b2b051a-bb96-41ed-816d-ab791033784d" />



## OOTD 피드 아키텍처 (CQRS)
<img width="908" height="445" alt="image" src="https://github.com/user-attachments/assets/6e681ec3-e7bf-4470-9e99-42eb6a611fc6" />

## 인기 피드(Hot Feed) 아키텍처 (Redis ZSet)
<img width="715" height="290" alt="image" src="https://github.com/user-attachments/assets/eaf2ee0f-3f2d-4624-9151-42bdc16a8c6e" />

## Outbox Pattern (Outbox + Polling)
<img width="720" height="318" alt="image" src="https://github.com/user-attachments/assets/490ef8db-e0f6-4468-9766-0af2ce1a509d" />
<img width="720" height="306" alt="image" src="https://github.com/user-attachments/assets/a69ea5f4-ad87-438e-aa92-b62533fd349c" />

## Elasticsearch 색인 전략 (전체색인 + 부분색인을 통한 데이터 동기화)
#### 전체 색인 아키텍처 (Spring Batch 활용)
<img width="433" height="300" alt="image" src="https://github.com/user-attachments/assets/c0cd2b55-ce04-46b7-ab72-319246a08279" />

#### 부분 색인 아키텍처 (Kafka, MongoDB 활용)
<img width="936" height="300" alt="image" src="https://github.com/user-attachments/assets/b5e574ef-7661-4b83-b6f2-9d1eec4ebf38" />


