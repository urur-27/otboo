package com.team3.otboo.domain.follow.repository;

import com.team3.otboo.domain.follow.entity.Follow;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {

	boolean existsByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

	// TODO: 인덱스 써서 쿼리 최적화 하기 .
	@Query(
		value = "select count(*) from follows where follower_id = :userId",
		nativeQuery = true
	)
	Integer countFollowings(@Param("userId") UUID userId);

	// 첫 페이지 조회용
	@Query(
		value = "select f.id, f.created_at, f.followee_id, f.follower_id "
			+ "from follows f "
			+ "where f.follower_id = :followerId "
			+ "order by f.created_at DESC, f.id DESC "
			+ "limit :limit",
		nativeQuery = true
	)
	List<Follow> getFollowings(
		@Param("followerId") UUID followerId,
		@Param("limit") Integer limit,
		@Param("nameLike") String nameLike
	);


	@Query(
		value = "select f.id, f.created_at, f.followee_id, f.follower_id "
			+ "from follows f "
			+ "where f.follower_id = :followerId "
			+ "and (f.created_at < :createdAt or "
			+ "(f.created_at = :createdAt and f.id < :cursorId)"
			+ ") order by f.created_at DESC, f.id DESC "
			+ "limit :limit",
		nativeQuery = true
	)
	List<Follow> getFollowings(
		@Param("followerId") UUID followerId,
		@Param("createdAt") LocalDateTime createdAt, // cursor
		@Param("cursorId") UUID cursorId, // cursorId
		@Param("limit") Integer limit,
		@Param("nameLike") String nameLike
	);

	// 보통 count 쿼리의 결과는 Long 으로 받는 것이 좋음. BIGINT 로 변환해 주는 경우가 많아서 .
	@Query(
		value = "select count(*) from follows f where followee_id = :userId",
		nativeQuery = true
	)
	Integer countFollowers(@Param("userId") UUID userId);

	// 첫페이지 조회용
	@Query(
		value = "select f.id, f.created_at, f.followee_id, f.follower_id "
			+ "from follows f "
			+ "where f.followee_id = :followeeId "
			+ "order by f.created_at DESC, f.id DESC "
			+ "limit :limit",
		nativeQuery = true
	)
	List<Follow> getFollowers(
		@Param("followeeId") UUID followeeId,
		@Param("limit") Integer limit,
		@Param("nameLike") String nameLike
	);


	@Query(
		value = "select f.id, f.created_at, f.followee_id, f.follower_id "
			+ "from follows f "
			+ "where f.followee_id = :followeeId "
			+ "and (f.created_at < :createdAt or "
			+ "(f.created_at = :createdAt and f.id < :cursorId)"
			+ ") order by f.created_at DESC, f.id DESC "
			+ "limit :limit",
		nativeQuery = true
	)
	List<Follow> getFollowers(
		@Param("followeeId") UUID followeeId,
		@Param("createdAt") LocalDateTime createdAt,
		@Param("cursorId") UUID cursorId,
		@Param("limit") Integer limit,
		@Param("nameLike") String nameLike
	);
}
