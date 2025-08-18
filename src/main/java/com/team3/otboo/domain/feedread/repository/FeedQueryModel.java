package com.team3.otboo.domain.feedread.repository;

import com.team3.otboo.common.event.payload.CommentCreatedEventPayload;
import com.team3.otboo.common.event.payload.FeedLikedEventPayload;
import com.team3.otboo.common.event.payload.FeedUnlikedEventPayload;
import com.team3.otboo.common.event.payload.FeedUpdatedEventPayload;
import com.team3.otboo.domain.feed.dto.AuthorDto;
import com.team3.otboo.domain.feed.dto.FeedDto;
import com.team3.otboo.domain.feed.dto.OotdDto;
import com.team3.otboo.domain.weather.dto.WeatherSummaryDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
public class FeedQueryModel {

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

	// feed 생성시, kafka listener 에서 이벤트 받아서 FeedDto 생성하고, FeedQueryModel 생성
	public static FeedQueryModel create(FeedDto feedDto) {
		FeedQueryModel feedQueryModel = new FeedQueryModel();
		feedQueryModel.id = feedDto.id();
		feedQueryModel.createdAt = feedDto.createdAt();
		feedQueryModel.updatedAt = feedDto.updatedAt();
		feedQueryModel.author = feedDto.author();
		feedQueryModel.weather = feedDto.weather();
		feedQueryModel.ootds = feedDto.ootds();
		feedQueryModel.content = feedDto.content();
		feedQueryModel.likeCount = feedDto.likeCount();
		feedQueryModel.commentCount = feedDto.commentCount();
		feedQueryModel.likedByMe = feedDto.likedByMe();
		return feedQueryModel;
	}

	// payload 내용을 그대로 이벤트 처리 순서가 꼬이면 문제가 발생함 .
	public void updateBy(CommentCreatedEventPayload payload) {
		this.commentCount = payload.getCommentCount();
	}

	public void updateBy(FeedLikedEventPayload payload) {
		this.likeCount = payload.getLikeCount();
	}

	public void updateBy(FeedUnlikedEventPayload payload) {
		this.likeCount = payload.getLikeCount();
	}

	public void updateBy(FeedUpdatedEventPayload payload) {
		this.content = payload.getContent();
		this.updatedAt = payload.getUpdatedAt();
	}
}
