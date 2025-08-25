package com.team3.otboo.domain.hot.controller;

import com.team3.otboo.domain.feed.dto.FeedDto;
import com.team3.otboo.domain.hot.service.HotFeedService;
import com.team3.otboo.domain.user.user_details.CustomUserDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HotFeedController {

	private final HotFeedService hotFeedService;

	// 날짜 받아서 그날의 hot feed 를 반환함 .
	@GetMapping("/api/hot-feeds/feeds/date/{dateStr}")
	public ResponseEntity<List<FeedDto>> readAll(
		@PathVariable("dateStr") String dateStr,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		List<FeedDto> response = hotFeedService.readAll(dateStr, userDetails.getId());
		log.info("[HotFeedController.readAll] response.size(): {}", response.size());
		return ResponseEntity.ok(response);
	}
}
