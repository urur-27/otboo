package com.team3.otboo.domain.hot.common.outboxMessageRelay;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, UUID> {

	List<Outbox> findAllByCreatedAtLessThanEqualOrderByCreatedAtAsc(
		Instant instant,
		Pageable pageable);
}
