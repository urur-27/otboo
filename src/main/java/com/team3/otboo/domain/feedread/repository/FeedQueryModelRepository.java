package com.team3.otboo.domain.feedread.repository;

import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toMap;

import com.team3.otboo.common.dataSerializer.DataSerializer;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedQueryModelRepository {

	private final StringRedisTemplate redisTemplate;

	// feed-read::feed::{feedId}
	private static final String KEY_FORMAT = "feed-read::feed::%s";

	public void create(FeedQueryModel feedQueryModel, Duration ttl) {
		// 직렬화해서 redis 에 저장하기 .
		redisTemplate.opsForValue()
			.set(generateKey(feedQueryModel), DataSerializer.serialize(feedQueryModel), ttl);
	}

	public void update(FeedQueryModel feedQueryModel) {
		redisTemplate.opsForValue()
			.setIfPresent(generateKey(feedQueryModel), DataSerializer.serialize(feedQueryModel));
	}

	public void delete(UUID feedId) {
		redisTemplate.delete(generateKey(feedId));
	}

	public Optional<FeedQueryModel> read(UUID feedId) {
		return Optional.ofNullable(
				redisTemplate.opsForValue().get(generateKey(feedId)))
			.map(json -> DataSerializer.deserialize(json, FeedQueryModel.class));
	}

	public Map<UUID, FeedQueryModel> readAll(List<UUID> feedIds) {
		List<String> keyList = feedIds.stream().map(this::generateKey).toList();
		return redisTemplate.opsForValue().multiGet(keyList).stream()
			.filter(Objects::nonNull) // null 체크 해주고 .
			.map(json -> DataSerializer.deserialize(json, FeedQueryModel.class))
			.collect(toMap(FeedQueryModel::getId, identity()));
	}

	private String generateKey(FeedQueryModel feedQueryModel) {
		return generateKey(feedQueryModel.getId());
	}

	private String generateKey(UUID feedId) {
		return KEY_FORMAT.formatted(feedId);
	}
}
