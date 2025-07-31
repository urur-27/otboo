package com.team3.otboo.domain.dm.controller;

import com.team3.otboo.domain.dm.dto.DirectMessageDtoCursorResponse;
import com.team3.otboo.domain.dm.service.DirectMessageService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DirectMessageController {

	private final DirectMessageService directMessageService;

	@GetMapping("/api/direct-messages")
	public DirectMessageDtoCursorResponse getMessages(
		@RequestParam UUID userId
	) {
		// CustomUserDetails 구현 이후에 구현 ..
		return null;
	}
}
