package com.team3.otboo.domain.feedread.controller;

import com.team3.otboo.domain.feed.service.request.FeedListRequest;
import com.team3.otboo.domain.feed.service.response.FeedDtoCursorResponse;
import com.team3.otboo.domain.feedread.service.FeedReadService;
import com.team3.otboo.domain.feedread.service.response.FeedReadResponse;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.enums.SortDirection;
import com.team3.otboo.domain.user.user_details.CustomUserDetails;
import com.team3.otboo.domain.weather.enums.PrecipitationType;
import com.team3.otboo.domain.weather.enums.SkyStatus;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
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

	// redis 에서 무한 스크롤 가져오는 경우
	@GetMapping("/api/feeds/redis")
	public ResponseEntity<FeedDtoCursorResponse> readAllInfiniteScrollByRedis(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(value = "cursor", required = false) String cursor,
		@RequestParam(value = "idAfter", required = false) UUID idAfter,
		@RequestParam(value = "limit") Integer limit,
		@RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
		@RequestParam(value = "sortDirection", defaultValue = "DESCENDING") SortDirection sortDirection,
		@RequestParam(value = "keywordLike", required = false) String keywordLike,
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
		log.info("[FeedController.readAllInfiniteScroll] return redis data.");
		FeedDtoCursorResponse response = feedReadService.readAllInfiniteScroll(user.getId(),
			request);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/api/feeds/elasticsearch")
	public ResponseEntity<FeedDtoCursorResponse> readAllInfiniteScrollByES(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(value = "cursor", required = false) String cursor,
		@RequestParam(value = "idAfter", required = false) UUID idAfter,
		@RequestParam(value = "limit") Integer limit,
		@RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
		@RequestParam(value = "sortDirection", defaultValue = "DESCENDING") SortDirection sortDirection,
		@RequestParam(value = "keywordLike", required = false) String keywordLike,
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
		log.info("[FeedController.readAllInfiniteScroll] return elasticsearch data.");
		FeedDtoCursorResponse response = feedReadService.readAllInfiniteScrollByEs(user.getId(),
			request);
		return ResponseEntity.ok(response);
	}
}
