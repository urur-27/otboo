package com.team3.otboo.domain.dm.service;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventPayload;
import com.team3.otboo.common.event.EventType;
import com.team3.otboo.common.outboxMessageRelay.DirectMessageOutboxEvent;
import com.team3.otboo.common.outboxMessageRelay.Outbox;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DMOutboxEventPublisher {

	// 스프링 내부에 이벤트 시스템을 통해서 발행함 .
	private final ApplicationEventPublisher applicationEventPublisher;

	// 카프카로 바로 전송하지 않고 .. MessageRelay(중계 시스템) 에게 publish 함 .
	// MessageRelay 는 @TransactionalEventListener 를 통해 듣고 있다가 . 이벤트가 발생하면 적절한 처리를 한다 .
	public void publish(EventType eventType, EventPayload payload) {
		Outbox outbox = Outbox.create(
			eventType,
			Event.of(
				UUID.randomUUID(),
				eventType,
				payload
			).toJson() // payload 를 event 로 감싸서 전달
		);
		// 이벤트가 발생했다는 신호를 애플리케이션에 전파함 .
		// 여기서 DirectMessageOutboxEvent 를 전송하니까 . 해당 파라미터를 가진 EventListener 만 동작
		// payload 를 event 로 감싸고, event 를 DirectMessageOutboxEvent 로 감싼다 .
		log.info("[DmOutboxEventPublisher.publish] Send DM to DMMessageRelay");
		applicationEventPublisher.publishEvent(DirectMessageOutboxEvent.of(outbox));
	}
}
