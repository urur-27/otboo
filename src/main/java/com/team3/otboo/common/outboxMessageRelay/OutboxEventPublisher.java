package com.team3.otboo.common.outboxMessageRelay;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventPayload;
import com.team3.otboo.common.event.EventType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

	// 스프링 내부에 이벤트 시스템을 통해서 발행함 .
	private final ApplicationEventPublisher applicationEventPublisher;

	// MessageRelay 는 @TransactionalEventListener 를 통해 듣고 있다가 . 이벤트가 발생하면 적절한 처리를 한다 .
	public void publish(EventType eventType, EventPayload payload) {
		Outbox outbox = Outbox.create(
			eventType,
			Event.of(
				UUID.randomUUID(),
				eventType,
				payload
			).toJson()
		);
		
		applicationEventPublisher.publishEvent(OutboxEvent.of(outbox));
	}
}
