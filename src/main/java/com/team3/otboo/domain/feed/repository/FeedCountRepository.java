package com.team3.otboo.domain.feed.repository;

import com.team3.otboo.domain.feed.entity.FeedCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedCountRepository extends JpaRepository<FeedCount, Long> {

	@Query(
		value = "update feed_count set feed_count = feed_count + 1 where id = :id",
		nativeQuery = true

	)
	@Modifying
	int increase(@Param("id") Long id);

	@Query(
		value = "update feed_count set feed_count = feed_count - 1 where id = :id",
		nativeQuery = true
	)
	@Modifying
	int decrease(@Param("id") Long id);
}
