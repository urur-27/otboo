package com.team3.otboo.domain.feed.repository;

import com.team3.otboo.domain.feed.entity.Feed;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedRepository extends JpaRepository<Feed, UUID> {

	@Query(
		value = "select f.author_id from feeds f where f.id = :id",
		nativeQuery = true
	)
	Optional<UUID> findAuthorIdById(UUID feedId);
}
