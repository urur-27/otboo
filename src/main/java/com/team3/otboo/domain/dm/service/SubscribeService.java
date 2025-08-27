package com.team3.otboo.domain.dm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3.otboo.domain.dm.event.payload.DirectMessageSentPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscribeService implements MessageListener {

	private final SimpMessageSendingOperations messageTemplate;
	private final ObjectMapper objectMapper;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			String publishMessage = new String(message.getBody());
			DirectMessageSentPayload payload = objectMapper
				.readValue(publishMessage, DirectMessageSentPayload.class);

			log.info("[SubscribeService.onMessage] payload: {}", payload);
			messageTemplate.convertAndSend(
				"/sub/direct-messages_" + payload.getDmKey(),
				payload.getDirectMessageDto());
		} catch (Exception e) {
			log.error("[SubscribeService.onMessage] json 역직렬화 중 오류 발생");
		}
	}
}
