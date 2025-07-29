package com.team3.otboo.domain.feed.dto;


import com.team3.otboo.domain.weather.dto.WeatherDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record FeedDto(
	UUID id,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	AuthorDto author,
	WeatherDto weather,
	List<OotdDto> ootds,
	String content,
	Long likeCount,
	Integer CommentCount,
	Boolean likedByMe
) {
}
