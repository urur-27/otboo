package com.team3.otboo.domain.feed.repository;

import com.team3.otboo.domain.feed.entity.FeedLikeCount;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedLikeCountRepository extends JpaRepository<FeedLikeCount, UUID> {

	@Query(
		value = "update feed_like_count set like_count = like_count + 1 where feed_id = :feedId",
		nativeQuery = true
	)
	@Modifying
	int increase(@Param("feedId") UUID feedId);

	@Query(
		value = "update feed_like_count set like_count = like_count - 1 where feed_id = :feedId",
		nativeQuery = true
	)
	@Modifying
	int decrease(@Param("feedId") UUID feedId);

	@Query(
		value = "update feed_like_count "
			+ "set like_count = like_count + 1 "
			+ "where feed_id = :feedId "
			+ "returning like_count",
		nativeQuery = true
	)
	@Modifying
	Long increaseAndGet(@Param("feedId") UUID feedId);

	@Query(
		value = "SELECT * FROM feed_like_count WHERE feed_id IN (:ids)",
		nativeQuery = true
	)
	List<FeedLikeCount> findAllByIdIn(@Param("ids") List<UUID> ids);
}
