package com.team3.otboo.domain.hot.repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HotFeedListRepository {

	private final StringRedisTemplate redisTemplate;

	// hot-feed::list::{yyyyMMdd} -> 그날의 hot article
	private static final String KEY_FORMAT = "hot-feed::list::%s";

	private static final DateTimeFormatter TIME_FORMATTER =
		DateTimeFormatter.ofPattern("yyyyMMdd");

	public void add(UUID feedId, LocalDateTime time, Long score, Long limit, Duration ttl) {
		// RedisTemplate pipeline 실행을 시작함. RedisConnection -> redis 연결에 대한 low-level 접근을 제공 ..
		redisTemplate.executePipelined((RedisCallback<?>) action -> {
			StringRedisConnection conn = (StringRedisConnection) action;
			String key = generateKey(time);
			conn.zAdd(key, score, String.valueOf(feedId)); // 점수와 feedId 추가 .
			conn.zRemRange(key, 0, -limit - 1); // 상위 limit 개 유지
			conn.expire(key, ttl.toSeconds());
			return null; // 모든 명령어 전송 .
		});
	}

	public void remove(LocalDateTime time, UUID feedId) {
		redisTemplate.opsForZSet().remove(generateKey(time), String.valueOf(feedId));
	}

	// 인기글을 표시하는 방법으로 구현 하려면 .. readAll 인기글을 불러서 2시간 정도마다 스케줄러로 isHot 을 갱신해준다 .
	public List<String> readAll(String dateStr) {
		return redisTemplate.opsForZSet()
			.reverseRangeWithScores(generateKey(dateStr), 0, -1)
			.stream()
			.map(TypedTuple::getValue)
			.toList();
	}

	private String generateKey(LocalDateTime time) {
		return generateKey(TIME_FORMATTER.format(time));
	}

	private String generateKey(String dateStr) {
		return KEY_FORMAT.formatted(dateStr);
	}
}
