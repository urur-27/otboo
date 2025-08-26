package com.team3.otboo.domain.feed.service;

import com.team3.otboo.common.outboxMessageRelay.Outbox;
import com.team3.otboo.common.outboxMessageRelay.OutboxEvent;
import com.team3.otboo.common.outboxMessageRelay.OutboxRepository;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeedMessageRelay {

	private final OutboxRepository outboxRepository; // outbox repository 에 서 주기적으로 미전송 데이터 가져옴
	private final KafkaTemplate<String, String> kafkaTemplate;
	// commit 직전에 outbox 를 생성하고 . commit 직후에 message relay 에게 바로 OutboxEvent 전달.

	// spring 은 AOP 프록시를 통해 @Transactional 의 전후를 감시함 (이벤트를 발행한 쪽의 트랜잭션 상태에 자신을 맞춤)
	// 이벤트를 발행시킨 작업의 트랜잭션이 특정 상태가 될때까지 기다렸다가 실행됨 .
	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void createOutbox(OutboxEvent outboxEvent) {
		outboxRepository.save(outboxEvent.getOutbox());
	}

	@Async("messageRelayPublishEventExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void publish(OutboxEvent outboxEvent) {
		publishEvent(outboxEvent.getOutbox());
	}

	private void publishEvent(Outbox outbox) {
		try {
			kafkaTemplate.send(
				outbox.getEventType().getTopic(),
				outbox.getPayload()
			).get(1, TimeUnit.SECONDS); // 1초 동안 전송 완료 메시지를 기다림 .

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
//		log.info("[FeedMessageRelay] Polling outbox messages. size={}", outboxes.size());

		for (Outbox outbox : outboxes) {
			publishEvent(outbox);
		}
	}

}
