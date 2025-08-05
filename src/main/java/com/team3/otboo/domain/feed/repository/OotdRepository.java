package com.team3.otboo.domain.feed.repository;

import com.team3.otboo.domain.feed.entity.Ootd;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OotdRepository extends JpaRepository<Ootd, UUID> {

	@Query(
		value = "select o.clothes_id from ootds o where o.feed_id = :feedId ",
		nativeQuery = true
	)
	List<UUID> findClothesIdsByFeedId(UUID feedId);

	@Query(
		value = "delete from ootds where feed_id = :feedId",
		nativeQuery = true
	)
	@Modifying
	void deleteAllByFeedId(
		@Param("feedId") UUID feedId
	);
}
