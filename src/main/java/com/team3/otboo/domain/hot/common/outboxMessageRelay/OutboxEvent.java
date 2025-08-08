package com.team3.otboo.domain.hot.common.outboxMessageRelay;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class OutboxEvent {

	private Outbox outbox; // outbox 를 OutboxEvent 로 감싸서 kafka 로 전송.

	public static OutboxEvent of(Outbox outbox) {
		OutboxEvent outboxEvent = new OutboxEvent();
		outboxEvent.outbox = outbox;
		return outboxEvent;
	}
}
