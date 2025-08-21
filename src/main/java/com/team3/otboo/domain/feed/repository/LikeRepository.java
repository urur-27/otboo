package com.team3.otboo.domain.feed.repository;

import com.team3.otboo.domain.feed.entity.Like;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, UUID> {

	@Query(
		value = "select * from likes where user_id = :userId and feed_id = :feedId",
		nativeQuery = true
	)
	Optional<Like> findByUserIdAndFeedId(UUID userId, UUID feedId);

	boolean existsByUserIdAndFeedId(UUID userId, UUID id);

	@Query(
		value = "delete from likes where feed_id = :feedId",
		nativeQuery = true
	)
	@Modifying
	void deleteAllByFeedId(
		@Param("feedId") UUID feedId
	);

	@Query(
		value = "delete from likes where feed_id = :feedId and user_id = :userId",
		nativeQuery = true
	)
	@Modifying
	void deleteByUserIdAndFeedId(
		@Param("userId") UUID userId,
		@Param("feedId") UUID feedId
	);

	@Query(
		value = "select feed_id from likes where user_id = :userId and feed_id in :feedIds",
		nativeQuery = true
	)
	Set<UUID> findLikedFeedIdsByUserAndFeedIn(
		@Param("userId") UUID userId,
		@Param("feedIds") List<UUID> feedIds
	);
}
