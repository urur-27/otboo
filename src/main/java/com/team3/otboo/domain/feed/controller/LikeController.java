package com.team3.otboo.domain.feed.controller;

import com.team3.otboo.domain.feed.dto.FeedDto;
import com.team3.otboo.domain.feed.service.LikeService;
import com.team3.otboo.domain.user.user_details.CustomUserDetails;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LikeController {

	private final LikeService likeService;

	@PostMapping("/api/feeds/{feedId}/like")
	public ResponseEntity<FeedDto> like(
		@PathVariable("feedId") UUID feedId,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		likeService.like(userDetails.getId(), feedId);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/api/feeds/{feedId}/like")
	public ResponseEntity<Void> unlike(
		@PathVariable("feedId") UUID feedId,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		likeService.unlike(userDetails.getId(), feedId);
		return ResponseEntity.noContent().build();
	}

	// test 용 api
	@GetMapping("/api/feeds/{feedId}/like")
	public ResponseEntity<Long> count(
		@PathVariable("feedId") UUID feedId
	) {
		Long count = likeService.count(feedId);
		return ResponseEntity.ok(count);
	}
}
