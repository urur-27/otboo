package com.team3.otboo.domain.feedread.client;

import com.team3.otboo.domain.feed.service.CommentService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentClient {

	private final CommentService commentService;

	public Long count(UUID feedId) {
		return commentService.count(feedId);
	}
}
