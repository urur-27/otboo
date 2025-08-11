package com.team3.otboo.domain.dm.controller;

import com.team3.otboo.domain.dm.service.DirectMessageService;
import com.team3.otboo.domain.dm.service.PublishService;
import com.team3.otboo.domain.dm.service.request.DirectMessageCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class StompController {

	private final DirectMessageService directMessageService;
	private final PublishService publishService;

	// /pub/direct_messages_send 로 메시지 전송하면 여기로 온다.
	@MessageMapping("/direct-messages_send")
	public void sendDirectMessage(
		@Payload DirectMessageCreateRequest request
	) {
		directMessageService.save(request);
	}
}
