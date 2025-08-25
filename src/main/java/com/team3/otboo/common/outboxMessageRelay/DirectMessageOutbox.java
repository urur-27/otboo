package com.team3.otboo.common.outboxMessageRelay;

import com.team3.otboo.common.event.EventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;

@Table(name = "direct_message_outbox")
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DirectMessageOutbox {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	@CreatedDate
	private Instant createdAt;
	@Enumerated(EnumType.STRING)
	private EventType eventType;

	@Column(columnDefinition = "TEXT")
	private String payload;


	public static DirectMessageOutbox create(EventType eventType, String payload) {
		DirectMessageOutbox outbox = new DirectMessageOutbox();
		outbox.eventType = eventType;
		outbox.payload = payload;
		return outbox;
	}
}
