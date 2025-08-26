package com.team3.otboo.common.outboxMessageRelay;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, UUID> {

	List<Outbox> findAllByCreatedAtLessThanEqualOrderByCreatedAtAsc(
		Instant instant,
		Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Outbox> findAndLockById(UUID id);
}
