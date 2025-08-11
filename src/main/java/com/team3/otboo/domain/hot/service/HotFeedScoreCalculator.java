package com.team3.otboo.domain.hot.service;

import com.team3.otboo.domain.feed.entity.FeedCommentCount;
import com.team3.otboo.domain.feed.entity.FeedLikeCount;
import com.team3.otboo.domain.feed.repository.FeedCommentCountRepository;
import com.team3.otboo.domain.feed.repository.FeedLikeCountRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HotFeedScoreCalculator {

	private final FeedLikeCountRepository feedLikeCountRepository;
	private final FeedCommentCountRepository feedCommentCountRepository;

	// 가중치 like, comment 3:1
	private static final long FEED_LIKE_COUNT_WEIGHT = 3;
	private static final long FEED_COMMENT_COUNT_WEIGHT = 1;

	// TODO: view service 만든 후 구현 .
	private static final long FEED_VIEW_COUNT_WEIGHT = 1;

	public long calculate(UUID feedId) {
		Long likeCount = feedLikeCountRepository.findById(feedId)
			.map(FeedLikeCount::getLikeCount)
			.orElse(0L);
		Long commentCount = feedCommentCountRepository.findById(feedId)
			.map(FeedCommentCount::getCommentCount)
			.orElse(0L);

		return likeCount * FEED_LIKE_COUNT_WEIGHT
			+ commentCount * FEED_COMMENT_COUNT_WEIGHT;
	}
}
