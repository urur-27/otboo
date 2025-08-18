package com.team3.otboo.domain.feed.service.response;

import com.team3.otboo.domain.feed.entity.Feed;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;

@Data
public class FeedResponse {

	private UUID id;
	private Instant createdAt;
	private Instant updatedAt;
	private UUID authorId;
	private UUID weatherId;
	private String content;

	public static FeedResponse from(Feed feed) {
		FeedResponse response = new FeedResponse();
		response.id = feed.getId();
		response.createdAt = feed.getCreatedAt();
		response.updatedAt = feed.getUpdatedAt();
		response.authorId = feed.getAuthorId();
		response.weatherId = feed.getWeatherId();
		response.content = feed.getContent();
		return response;
	}
}
