package com.team3.otboo.domain.dm.service;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventPayload;
import com.team3.otboo.common.outboxMessageRelay.DirectMessageOutboxEvent;
import com.team3.otboo.common.outboxMessageRelay.Outbox;
import com.team3.otboo.common.outboxMessageRelay.OutboxRepository;
import com.team3.otboo.domain.dm.event.payload.DirectMessageSentPayload;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class DMMessageRelay {

	private final OutboxRepository outboxRepository; // outbox repository 에 서 주기적으로 미전송 데이터 가져옴

	private final PublishService publishService;

	// 이벤트를 발행시킨 작업의 트랜잭션이 특정 상태가 될때까지 기다렸다가 실행됨 .
	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void createOutbox(DirectMessageOutboxEvent outboxEvent) {
		outboxRepository.save(outboxEvent.getOutbox());
	}

	@Async("messageRelayPublishEventExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void publish(DirectMessageOutboxEvent outboxEvent) {
		publishEvent(outboxEvent.getOutbox());
	}

	private void publishEvent(Outbox outbox) {
		try {
			Event<EventPayload> event = Event.fromJson(outbox.getPayload());
			// event 의 필드가 인터페이스여서 .. 실제 어떤 클래스로 객체를 만들지 정해줘야함 .
			DirectMessageSentPayload payload = (DirectMessageSentPayload) event.getPayload();

			publishService.publish(payload);
			outboxRepository.delete(outbox);
		} catch (Exception e) {
			log.error("[MessageRelay.publishEvent] outbox={}", outbox, e);
		}
	}

	@Scheduled(
		fixedDelay = 10,
		initialDelay = 5,
		timeUnit = TimeUnit.SECONDS
	)
	public void publishPendingEvents() {
		List<Outbox> outboxes = outboxRepository
			.findAllByCreatedAtLessThanEqualOrderByCreatedAtAsc(
				Instant.now().minusSeconds(10), // 생성된지 10초가 지난 메시지 가져옴 .
				Pageable.ofSize(100)
			);
//		log.info("[MessageRelay] Polling outbox messages. size={}", outboxes.size());

		for (Outbox outbox : outboxes) {
			publishEvent(outbox);
		}
	}
}
