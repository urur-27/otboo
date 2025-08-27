package com.team3.otboo.domain.dm.service;

import com.team3.otboo.domain.dm.event.payload.DirectMessageSentPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PublishService {

	private final RedisTemplate<String, Object> redisTemplate;

	public PublishService(@Qualifier("chatPubSub") RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void publish(DirectMessageSentPayload payload) {
		log.info("[PublishService.publish] Send DM to Redis");
		redisTemplate.convertAndSend("direct-messages", payload);
	}
}
