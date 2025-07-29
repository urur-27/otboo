package com.team3.otboo.domain.feed.dto;


import com.team3.otboo.domain.weather.dto.WeatherDto;
import java.time.LocalDateTime;
import java.util.UUID;

public record FeedDto(
	UUID id,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	AuthorDto author,
	WeatherDto weather,
	List<OotodDto> ootds,
	String content,
	Long likeCount,
	Integer CommentCount,
	Boolean likedByMe
) {
}
