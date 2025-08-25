package com.team3.otboo.common.outboxMessageRelay;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class DirectMessageOutboxEvent {

	private DirectMessageOutbox outbox;

	public static DirectMessageOutboxEvent of(DirectMessageOutbox outbox) {
		DirectMessageOutboxEvent directMessageOutboxEvent = new DirectMessageOutboxEvent();
		directMessageOutboxEvent.outbox = outbox;
		return directMessageOutboxEvent;
	}
}
