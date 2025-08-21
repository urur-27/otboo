package com.team3.otboo.domain.feed.repository;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedViewCountRepository {

	private final StringRedisTemplate redisTemplate;

	// view::feed::{feed_id}::view_count
	private static final String KEY_FORMAT = "view::feed::%s::view_count";

	public Long read(UUID feedId) {
		String result = redisTemplate.opsForValue().get(generateKey(feedId));
		return result == null ? 0L : Long.parseLong(result);
	}

	public Long increase(UUID feedId) {
		return redisTemplate.opsForValue().increment(generateKey(feedId));
	}

	private String generateKey(UUID feedId) {
		return KEY_FORMAT.formatted(feedId);
	}
}
