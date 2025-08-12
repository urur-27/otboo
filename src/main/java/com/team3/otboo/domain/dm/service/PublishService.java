package com.team3.otboo.domain.dm.service;

import com.team3.otboo.domain.dm.event.payload.DirectMessageSentPayload;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PublishService {

	private final RedisTemplate<String, Object> redisTemplate;

	public PublishService(@Qualifier("chatPubSub") RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void publish(DirectMessageSentPayload payload) {
		redisTemplate.convertAndSend("direct-messages", payload);
	}
}
