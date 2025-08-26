package com.team3.otboo.domain.dm.repository;

import com.team3.otboo.domain.dm.entity.DirectMessage;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID> {

	@Query(
		value = "select d.id, d.created_at, d.updated_at, d.sender_id, d.receiver_id, d.content "
			+ "from direct_messages d "
			+ "where ((d.sender_id = :userId and d.receiver_id = :currentUserId) "
			+ "or (d.sender_id = :currentUserId and d.receiver_id = :userId)) "
			+ "order by d.created_at desc, d.id desc "
			+ "limit :limit"
		,
		nativeQuery = true
	)
	List<DirectMessage> getDirectMessages(
		@Param("userId") UUID userId,
		@Param("currentUserId") UUID currentUserId,
		@Param("limit") int limit
	);

	@Query(
		value =
			"select d.id, d.created_at, d.updated_at, d.sender_id, d.receiver_id, d.content "
				+ "from direct_messages d "
				+ "where ((d.sender_id = :userId and d.receiver_id = :currentUserId) "
				+ "or (d.sender_id = :currentUserId and d.receiver_id = :userId)) "
				+ "and (d.created_at < :lastCreatedAt "
				+ "or (d.created_at = :lastCreatedAt and d.id < :idAfter)) "
				+ "order by d.created_at desc, d.id desc "
				+ "limit :limit",
		nativeQuery = true
	)
	List<DirectMessage> getDirectMessages(
		@Param("userId") UUID userId,
		@Param("currentUserId") UUID currentUserId,
		@Param("lastCreatedAt") Instant lastCreatedAt,
		@Param("idAfter") UUID idAfter,
		@Param("limit") int limit
	);
}
