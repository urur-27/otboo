package com.team3.otboo.domain.dm.controller;

import com.team3.otboo.domain.dm.dto.DirectMessageDtoCursorResponse;
import com.team3.otboo.domain.dm.service.DirectMessageService;
import com.team3.otboo.domain.user.service.CustomUserDetailsService.CustomUserDetails;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DirectMessageController {

	private final DirectMessageService directMessageService;

	@GetMapping("/api/direct-messages")
	public ResponseEntity<DirectMessageDtoCursorResponse> getMessages(
		@RequestParam UUID userId,
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false) UUID idAfter,
		@RequestParam int limit
	) {
		DirectMessageDtoCursorResponse response = directMessageService
			.getDirectMessages(userId, userDetails.getId(), cursor, idAfter, limit);

		return ResponseEntity.ok(response);
	}
}
