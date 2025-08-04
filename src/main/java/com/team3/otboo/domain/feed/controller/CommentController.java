package com.team3.otboo.domain.feed.controller;

import com.team3.otboo.domain.feed.dto.CommentDto;
import com.team3.otboo.domain.feed.dto.CommentDtoCursorResponse;
import com.team3.otboo.domain.feed.service.CommentService;
import com.team3.otboo.domain.feed.service.request.CommentCreateRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentController {

	private final CommentService commentService;

	@GetMapping("/api/feeds/{feedId}/comments")
	public ResponseEntity<CommentDtoCursorResponse> getComments(
		@PathVariable UUID feedId,
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false) UUID idAfter,
		@RequestParam(defaultValue = "10") int limit
	) {
		CommentDtoCursorResponse response =
			commentService.getComments(feedId, cursor, idAfter, limit);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/api/feeds/{feedId}/comments")
	public ResponseEntity<CommentDto> create(
		@RequestBody CommentCreateRequest request
	) {
		CommentDto commentDto = commentService.create(request);

		return ResponseEntity.ok(commentDto);
	}
}
