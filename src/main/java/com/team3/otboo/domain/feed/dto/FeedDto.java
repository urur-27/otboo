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
	List<OotdDto> ootds, // 중간 테이블 이름을 왜 ootd로 했을까
	String content,
	Long likeCount,
	Integer commentCount,
	Boolean likedByMe
) {

}
