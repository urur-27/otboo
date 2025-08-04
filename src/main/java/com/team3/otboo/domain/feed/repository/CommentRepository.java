package com.team3.otboo.domain.feed.repository;

import com.team3.otboo.domain.feed.entity.Comment;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

	@Query(
		value = "select c.id, c.created_at, c.updated_at, c.feed_id, c.author_id, c.content "
			+ "from comments c "
			+ "where c.feed_id = :feedId "
			+ "order by c.created_at ASC, c.id ASC "
			+ "limit :limit",
		nativeQuery = true
	)
	List<Comment> findAll(
		@Param("feedId") UUID feedId,
		@Param("limit") Integer limit
	);

	@Query(
		value = "select c.id, c.created_at, c.updated_at, c.feed_id, c.author_id, c.content "
			+ "from comments c "
			+ "where c.feed_id = :feedId "
			+ "and (c.created_at > :createdAt or " // 값이 큰 데이터가 다음에 오는 데이터
			+ "(c.created_at = :createdAt and c.id > :idAfter)"
			+ ") order by c.created_at ASC, c.id ASC "
			+ "limit :limit",
		nativeQuery = true
	)
	List<Comment> findAll(
		@Param("feedId") UUID feedId,
		@Param("createdAt") Instant createdAt, // cursor
		@Param("idAfter") UUID idAfter, // lastCommentId,
		@Param("limit") Integer limit
	);

	@Query(
		value = "delete from comments where feed_id = :feedId",
		nativeQuery = true
	)
	@Modifying
	void deleteAllByFeedId(
		@Param("feedId") UUID feedId
	);
}
