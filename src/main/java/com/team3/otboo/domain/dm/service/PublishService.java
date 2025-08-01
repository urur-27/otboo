package com.team3.otboo.domain.dm.service;

import com.team3.otboo.domain.dm.dto.DirectMessageSendPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublishService {

	@Qualifier("chatPubSub")
	private final RedisTemplate<String, Object> redisTemplate;

	public void publish(DirectMessageSendPayload payload) {
		redisTemplate.convertAndSend("direct-messages", payload);
	}
}
