package com.team3.otboo.domain.feedread.service.response;

import com.team3.otboo.domain.feed.dto.AuthorDto;
import com.team3.otboo.domain.feed.dto.OotdDto;
import com.team3.otboo.domain.feedread.repository.FeedQueryModel;
import com.team3.otboo.domain.weather.dto.WeatherSummaryDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class FeedReadResponse {

	// 사실상 FeedDto 랑 같음 .
	private UUID id;
	private Instant createdAt;
	private Instant updatedAt;
	private AuthorDto author;
	private WeatherSummaryDto weather;
	private List<OotdDto> ootds;
	private String content;
	private Long likeCount;
	private Integer commentCount;
	private Boolean likedByMe;

	private Long viewCount;

//	private Long viewCount;

	public static FeedReadResponse from(FeedQueryModel feedQueryModel, Boolean likedByMe,
		Long viewCount) {
		FeedReadResponse response = new FeedReadResponse();
		response.id = feedQueryModel.getId();
		response.createdAt = feedQueryModel.getCreatedAt();
		response.updatedAt = feedQueryModel.getUpdatedAt();
		response.author = feedQueryModel.getAuthor();
		response.weather = feedQueryModel.getWeather();
		response.ootds = feedQueryModel.getOotds();
		response.content = feedQueryModel.getContent();
		response.likeCount = feedQueryModel.getLikeCount();
		response.commentCount = feedQueryModel.getCommentCount();
		response.likedByMe = likedByMe;
		response.viewCount = viewCount;
		return response;
	}

}
