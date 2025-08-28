package com.team3.otboo.domain.feed.repository;

import com.team3.otboo.domain.feed.entity.Feed;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedRepository extends JpaRepository<Feed, UUID> {

	@Query(
		value = "select f.author_id from feeds f where f.id = :id",
		nativeQuery = true
	)
	Optional<UUID> findAuthorIdById(
		@Param("id") UUID feedId
	);

	/**
	 * 주어진 UUID 목록에 해당하는 모든 Feed 엔티티를 조회합니다. Native SQL의 IN 절을 사용하여 한번의 쿼리로 모든 데이터를 가져옵니다.
	 *
	 * @param ids 조회할 Feed의 UUID 목록
	 * @return 조회된 Feed 엔티티 목록
	 */
	@Query(
		value = "SELECT * FROM feeds f WHERE f.id IN (:ids)",
		nativeQuery = true
	)
	List<Feed> findAllByIdIn(@Param("ids") List<UUID> ids);
}
