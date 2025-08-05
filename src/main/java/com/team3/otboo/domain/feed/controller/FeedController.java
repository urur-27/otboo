package com.team3.otboo.domain.feed.controller;

import com.team3.otboo.domain.feed.dto.FeedDto;
import com.team3.otboo.domain.feed.dto.FeedDtoCursorResponse;
import com.team3.otboo.domain.feed.service.FeedService;
import com.team3.otboo.domain.feed.service.request.FeedCreateRequest;
import com.team3.otboo.domain.feed.service.request.FeedListRequest;
import com.team3.otboo.domain.feed.service.request.FeedUpdateRequest;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.enums.SortDirection;
import com.team3.otboo.domain.user.service.CustomUserDetailsService.CustomUserDetails;
import com.team3.otboo.domain.weather.enums.PrecipitationType;
import com.team3.otboo.domain.weather.enums.SkyStatus;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FeedController {

	private final FeedService feedService;

	@PostMapping("/api/feeds")
	public ResponseEntity<FeedDto> create(
		@RequestBody FeedCreateRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		UUID userId = userDetails.getId();
		FeedDto feedDto = feedService.create(userId, request);
		return ResponseEntity.ok(feedDto);
	}

	// patch -> 부분 리소스 수정 .
	@PatchMapping("/api/feeds/{feedId}")
	public ResponseEntity<FeedDto> update(
		@PathVariable("feedId") UUID feedId,
		@RequestBody FeedUpdateRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		UUID userId = userDetails.getId();
		FeedDto feedDto = feedService.update(feedId, userId, request);
		return ResponseEntity.ok(feedDto);
	}

	@DeleteMapping("/api/feeds/{feedId}")
	public ResponseEntity<Void> delete(
		@PathVariable("feedId") UUID feedId
	) {
		feedService.delete(feedId);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/api/feeds")
	public ResponseEntity<FeedDtoCursorResponse> getFeeds(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(value = "cursor", required = false) String cursor,
		@RequestParam(value = "idAfter", required = false) UUID idAfter,
		@RequestParam(value = "limit") Integer limit,
		@RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
		@RequestParam(value = "sortDirection", defaultValue = "ASCENDING") SortDirection sortDirection,
		@RequestParam(value = "keyWordLike", required = false) String keywordLike,
		@RequestParam(value = "skyStatusEqual", required = false) SkyStatus skyStatusEqual,
		@RequestParam(value = "precipitationTypeEqual", required = false) PrecipitationType precipitationTypeEqual,
		@RequestParam(value = "authorIdEqual", required = false) UUID authorIdEqual
	) {
		User user = userDetails.getUser();
		FeedListRequest request = new FeedListRequest(
			cursor,
			idAfter,
			limit,
			sortBy,
			sortDirection,
			keywordLike,
			skyStatusEqual,
			precipitationTypeEqual,
			authorIdEqual
		);

		FeedDtoCursorResponse response = feedService.getFeeds(user.getId(), request);

		return ResponseEntity.ok(response);
	}
}
