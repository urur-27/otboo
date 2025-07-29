package com.team3.otboo.domain.feed.dto;

import java.util.UUID;

public record AuthorDto(
	UUID userId,
	String name,
	String profileImageUrl
) {

}
