package com.team3.otboo.domain.feed.dto;


import com.team3.otboo.domain.weather.dto.WeatherSummaryDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FeedDto(
	UUID id,
	Instant createdAt,
	Instant updatedAt,
	AuthorDto author,
	WeatherSummaryDto weather,
	List<OotdDto> ootds,
	String content,
	Long likeCount,
	Integer commentCount,
	Boolean likedByMe
	// Long viewCount
	// 바뀌는 데이터 -> content, likeCount, commentCount, likedByMe, viewCount
) {

}
