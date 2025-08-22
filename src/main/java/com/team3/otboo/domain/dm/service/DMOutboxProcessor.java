package com.team3.otboo.domain.dm.service;

import com.team3.otboo.common.dataSerializer.DataSerializer;
import com.team3.otboo.common.outboxMessageRelay.Outbox;
import com.team3.otboo.common.outboxMessageRelay.OutboxRepository;
import com.team3.otboo.domain.dm.event.payload.DirectMessageSentPayload;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DMOutboxProcessor {

	private final OutboxRepository outboxRepository;
	private final PublishService publishService; // DM 발행을 위한 서비스

	@Transactional
	public void processAndLock(UUID outboxId) {
		Optional<Outbox> outboxOptional = outboxRepository.findAndLockById(outboxId);

		if (outboxOptional.isEmpty()) {
			log.debug("[OutboxProcessor] Outbox message {} already processed.", outboxId);
			return;
		}

		Outbox outbox = outboxOptional.get();

		try {
			// 페이로드를 역직렬화하여 Redis에 발행합니다.
			DirectMessageSentPayload payload = DataSerializer.deserialize(
				outbox.getPayload(),
				DirectMessageSentPayload.class
			);

			// Kafka 대신 Redis PublishService를 호출합니다.
			if (payload != null) {
				publishService.publish(payload);
			}

			outboxRepository.delete(outbox);
		} catch (Exception e) {
			log.error("[OutboxProcessor] outbox={} 처리 실패, 롤백됩니다.", outbox, e);
			throw new RuntimeException(e);
		}
	}
}
