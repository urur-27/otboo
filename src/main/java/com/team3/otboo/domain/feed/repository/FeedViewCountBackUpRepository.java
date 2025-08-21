package com.team3.otboo.domain.feed.repository;

import com.team3.otboo.domain.feed.entity.FeedViewCount;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedViewCountBackUpRepository extends
	JpaRepository<FeedViewCount, UUID> {

	@Query(
		value = "update feed_view_count set view_count = :viewCount "
			+ "where feed_id = :feedId and view_count < :viewCount",
		nativeQuery = true
	)
	@Modifying
	int update(
		@Param("feedId") UUID feedId,
		@Param("viewCount") Long viewCount
	);
}
