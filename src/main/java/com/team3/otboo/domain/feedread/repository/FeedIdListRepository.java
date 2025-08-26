package com.team3.otboo.domain.feedread.repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedIdListRepository {

	private final StringRedisTemplate redisTemplate;

	// 지금은 피드 목록이 하나
	private static final String KEY_FORMAT = "feed-read::all::feed-list";

	public void add(UUID feedId, Instant createdAt, Long limit) {
		redisTemplate.executePipelined((RedisCallback<?>) action -> {
			StringRedisConnection conn = (StringRedisConnection) action;
			String key = generateKey();
			conn.zAdd(key, createdAt.toEpochMilli(), feedId.toString());
			conn.zRemRange(key, 0, -limit - 1); // limit -> 몇개 저장할지.
			return null; // redis call back 에서 반환하는 값은 반드시 null 이여야함 .
		});
	}

	public void delete(UUID feedId) {
		redisTemplate.opsForZSet().remove(generateKey(), feedId.toString());
	}

	// 가장 기본 정렬 (createdAt 을 통해 최신순 정렬)만 캐싱
	public List<UUID> readAllInfiniteScroll(Instant cursor, long limit) {
		// cursor 가 있으면 max 가 를 cursor 시간으로 바꿈 .
		// 최신글부터 max 를 줄여나가면서 과거글을 탐색함 .
		double max = (cursor == null) ? Double.POSITIVE_INFINITY : cursor.toEpochMilli();
		double min = Double.NEGATIVE_INFINITY;

		// max 값이 포함이 되므로 limit+1 해준 다음에 맨 앞 요소를 제거해야함 .
		Set<String> ids = redisTemplate.opsForZSet().reverseRangeByScore(
			generateKey(),
			min,
			max,
			0,
			cursor == null ? limit : limit + 1 // 커서 있으면 limit + 1 개 가져옴 .
		);

		if (ids == null || ids.isEmpty()) {
			return List.of();
		}

		if (cursor == null) { // 커서 없으면 그대로 반환 .. limit 개 만큼 가져왔으니까.
			return ids.stream()
				.map(UUID::fromString)
				.toList();
		} else { // 커서 있으면 맨 앞에 겹치는 요소는 버리고 1 ~ limit + 1개만 살리기 .
			return ids.stream()
				.skip(1) // 첫번째 요소는 skip
				.map(UUID::fromString)
				.toList();
		}
	}

	private String generateKey() {
		return KEY_FORMAT;
	}
}
