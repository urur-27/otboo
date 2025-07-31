package com.team3.otboo.domain.dm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3.otboo.domain.dm.dto.DirectMessageSendPayload;
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

			DirectMessageSendPayload payload = objectMapper
				.readValue(publishMessage, DirectMessageSendPayload.class);

			messageTemplate.convertAndSend(
				"/sub/direct-messages_" + payload.dmKey(),
				payload);
		} catch (Exception e) {
			log.error("[SubscribeService.onMessage]json 역직렬화 중 오류 발생");
		}
	}
}
