package com.team3.otboo.domain.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class SocialAccount {
    @Id
    UUID id;
    Social provider;
    UUID socialId;
    String nickname;
    String email;
    UUID userId;
    LocalDateTime createdAt;
}
