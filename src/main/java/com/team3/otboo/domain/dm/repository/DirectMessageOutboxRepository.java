package com.team3.otboo.domain.dm.repository;

import com.team3.otboo.common.outboxMessageRelay.DirectMessageOutbox;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DirectMessageOutboxRepository extends JpaRepository<DirectMessageOutbox, UUID> {

	List<DirectMessageOutbox> findAllByCreatedAtLessThanEqualOrderByCreatedAtAsc(
		Instant instant,
		Pageable pageable);
}
