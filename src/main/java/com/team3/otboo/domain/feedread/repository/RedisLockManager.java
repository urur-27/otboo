package com.team3.otboo.domain.feedread.repository;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisLockManager {

	private final StringRedisTemplate redisTemplate;

	/**
	 * 락을 획득합니다.
	 *
	 * @param key     락 키
	 * @param value   락 소유자를 식별하기 위한 값 (e.g., UUID)
	 * @param timeout 락 만료 시간
	 * @return 락 획득 성공 시 true, 실패 시 false
	 */
	public boolean acquireLock(String key, String value, Duration timeout) {
		// SET key value NX EX timeout
		Boolean success = redisTemplate.opsForValue()
			.setIfAbsent(key, value, timeout);
		return success != null && success;
	}

	/**
	 * 락이 존재하는지 확인합니다.
	 *
	 * @param key 락 키
	 * @return 락이 존재하면 true, 아니면 false
	 */
	public boolean isLockHeld(String key) {
		return redisTemplate.hasKey(key);
	}

	/**
	 * 락을 해제합니다.
	 *
	 * @param key   락 키
	 * @param value 락을 획득할 때 사용했던 값
	 * @return 락 해제 성공 시 true, 실패 시 false
	 */
	public boolean releaseLock(String key, String value) {
		// 현재 락을 소유한 클라이언트인지 확인
		String currentValue = redisTemplate.opsForValue().get(key);
		if (value.equals(currentValue)) {
			redisTemplate.delete(key);
			return true;
		}
		return false;
	}
}
