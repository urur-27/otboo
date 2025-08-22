package com.team3.otboo.domain.feedread.controller;

import com.team3.otboo.domain.feedread.service.FeedReadService;
import com.team3.otboo.domain.feedread.service.response.FeedReadResponse;
import com.team3.otboo.domain.user.user_details.CustomUserDetails;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FeedReadController {

	private final FeedReadService feedReadService;

	// 단일 피드 조회 기능 .
	@GetMapping("/api/feeds/{feedId}")
	public ResponseEntity<FeedReadResponse> read(
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable("feedId") UUID feedId
	) {
		UUID userId = customUserDetails.getId();
		FeedReadResponse response = feedReadService.read(feedId, userId);
		return ResponseEntity.ok(response);
	}
}
