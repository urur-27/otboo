package com.team3.otboo.common.outboxMessageRelay;

import com.team3.otboo.common.event.EventType;
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

@Table(name = "outbox")
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Outbox {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	@CreatedDate
	private Instant createdAt;
	@Enumerated(EnumType.STRING)
	private EventType eventType;
	private String payload; // 해당 이벤트가 어떤 값을 가지고 있는가 .
	private UUID shardKey;

	public static Outbox create(EventType eventType, String payload, UUID shardKey) {
		Outbox outbox = new Outbox();
		outbox.eventType = eventType;
		outbox.payload = payload;
		outbox.shardKey = shardKey;
		return outbox;
	}
}
