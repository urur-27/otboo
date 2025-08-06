package com.team3.otboo.domain.follow.controller;

import com.team3.otboo.domain.follow.dto.FollowDto;
import com.team3.otboo.domain.follow.dto.FollowSummaryDto;
import com.team3.otboo.domain.follow.service.FollowService;
import com.team3.otboo.domain.follow.service.request.FollowCreateRequest;
import com.team3.otboo.domain.follow.service.response.FollowListResponse;
import com.team3.otboo.domain.user.service.CustomUserDetailsService.CustomUserDetails;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FollowController {

	private final FollowService followService;

	@PostMapping("/api/follows")
	public ResponseEntity<FollowDto> follow(
		@RequestBody FollowCreateRequest request) {
		FollowDto followDto = followService.create(request);
		return ResponseEntity.ok(followDto);
	}

	@DeleteMapping("/api/follows/{followId}")
	public ResponseEntity<Void> unfollow(
		@PathVariable UUID followId
	) {
		followService.delete(followId);
		return ResponseEntity.noContent().build();
	}

	// 내가 팔로우 하고 있는 사람 목록 조회
	@GetMapping("/api/follows/followings")
	public ResponseEntity<FollowListResponse> getFollowings(
		@RequestParam UUID followerId, // 내 아이디
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false) UUID idAfter,
		@RequestParam Integer limit,
		@RequestParam(required = false) String nameLike // 비슷한 이름 필터링
	) {
		FollowListResponse response = followService.getFollowings(
			followerId, cursor, idAfter, limit, nameLike
		);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/api/follows/followers")
	public ResponseEntity<FollowListResponse> getFollowers(
		@RequestParam UUID followeeId,
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false) UUID idAfter,
		@RequestParam Integer limit,
		@RequestParam(required = false) String nameLike
	) {
		FollowListResponse response = followService.getFollowers(
			followeeId, cursor, idAfter, limit, nameLike
		);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/api/follows/summary")
	public ResponseEntity<FollowSummaryDto> getFollowSummary(
		@RequestParam("userId") UUID userId,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		UUID currentUserId = userDetails.getId();
		FollowSummaryDto response = followService.getFollowSummary(userId, currentUserId);

		return ResponseEntity.ok(response);
	}
}
