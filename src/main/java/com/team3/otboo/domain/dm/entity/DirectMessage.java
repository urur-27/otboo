package com.team3.otboo.domain.dm.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Table(name = "direct_messages")
@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DirectMessage extends BaseEntity {

	@Column(nullable = false)
	private UUID senderId;

	@Column(nullable = false)
	private UUID receiverId;

	@Column(nullable = false)
	private String content;

	public static DirectMessage create(UUID senderId, UUID receiverId, String content){
		DirectMessage directMessage = new DirectMessage();
		directMessage.senderId = senderId;
		directMessage.receiverId = receiverId;
		directMessage.content = content;

		return directMessage;
	}
}
