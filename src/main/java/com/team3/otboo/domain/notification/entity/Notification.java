package com.team3.otboo.domain.notification.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import com.team3.otboo.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recevioer_id", nullable = false)
  private User receiver;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, length = 500)
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationLevel level;

  private boolean isRead = false;

  @Builder
  public Notification(User receiver, String title, String content, NotificationLevel level) {
    this.receiver = receiver;
    this.title = title;
    this.content = content;
    this.level = level;
  }

}
