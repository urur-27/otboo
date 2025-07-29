package com.team3.otboo.domain.feed.controller;

import com.team3.otboo.domain.feed.dto.FeedDto;
import com.team3.otboo.domain.feed.service.FeedService;
import com.team3.otboo.domain.feed.service.request.FeedCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FeedController {

	private final FeedService feedService;

	@PostMapping("/api/feeds")
	public ResponseEntity<FeedDto> create(
		@RequestBody FeedCreateRequest request
	){
		FeedDto feedDto = feedService.create(request);
		return ResponseEntity.ok(feedDto);
	}
}
