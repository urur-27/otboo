package com.team3.otboo.domain.dm.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name = "direct_messages",
	indexes = {
		@Index(
			name = "idx_dm_users_created_id_asc",
			columnList = "sender_id, receiver_id, created_at, id"
		),
		@Index(
			name = "idx_dm_users_reverse_created_id_asc",
			columnList = "receiver_id, sender_id, created_at, id"
		)
	})
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

	public static DirectMessage create(UUID senderId, UUID receiverId, String content) {
		DirectMessage directMessage = new DirectMessage();
		directMessage.senderId = senderId;
		directMessage.receiverId = receiverId;
		directMessage.content = content;

		return directMessage;
	}
}
