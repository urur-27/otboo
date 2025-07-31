package com.team3.otboo.domain.feed.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentDto(
	UUID id,
	Instant createdAt,
	UUID feedId,
	AuthorDto author,
	String content
) {

}
