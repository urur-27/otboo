package com.team3.otboo.domain.feed.controller;

import com.team3.otboo.domain.feed.service.ViewService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ViewController {

	private final ViewService viewService;

	@PostMapping("/api/feeds/{feedId}/view")
	public ResponseEntity<Long> increase(
		@PathVariable("feedId") UUID feedId
	) {
		log.info("[ViewController.increase()]");
		Long count = viewService.increase(feedId);
		return ResponseEntity.ok(count);
	}

	@GetMapping("/api/feeds/{feedId}/view")
	public ResponseEntity<Long> count(
		@PathVariable("feedId") UUID feedId
	) {
		log.info("[ViewController.count()]");
		Long count = viewService.count(feedId);
		return ResponseEntity.ok(count);
	}
}
