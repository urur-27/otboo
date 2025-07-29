package com.team3.otboo.domain.feed.service.request;


import java.util.List;
import java.util.UUID;

public record FeedCreateRequest(
	UUID authorId,
	UUID weatherId,
	List<UUID> clothesIds,
	String content
) {

}
