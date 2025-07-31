package com.team3.otboo.domain.feed.repository;

import com.team3.otboo.domain.feed.entity.FeedCommentCount;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedCommentCountRepository extends JpaRepository<FeedCommentCount, UUID> {

	@Query(
		value = "update feed_comment_count set comment_count = comment_count + 1 where feed_id = :feedId",
		nativeQuery = true
	)
	@Modifying
	int increase(@Param("feedId") UUID feedId);

	@Query(
		value = "update feed_comment_count set comment_count = comment_count - 1 where feed_id = :feedId",
		nativeQuery = true
	)
	@Modifying
	int decrease(@Param("feedId") UUID feedId);
}
