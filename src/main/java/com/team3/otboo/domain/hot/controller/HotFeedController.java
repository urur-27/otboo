package com.team3.otboo.domain.hot.controller;

import com.team3.otboo.domain.feed.dto.FeedDto;
import com.team3.otboo.domain.hot.service.HotFeedService;
import com.team3.otboo.domain.user.user_details.CustomUserDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class HotFeedController {

	private final HotFeedService hotFeedService;

	// 날짜 받아서 그날의 hot feed 를 반환함 .
	@GetMapping("/api/hot-feeds/feeds/date/{dateStr}")
	public List<FeedDto> readAll(
		@PathVariable("dateStr") String dateStr,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		return hotFeedService.readAllFeeds(dateStr, userDetails.getId());
	}
}
