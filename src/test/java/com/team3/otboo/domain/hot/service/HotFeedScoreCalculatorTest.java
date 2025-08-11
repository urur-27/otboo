package com.team3.otboo.domain.hot.service;

import static org.mockito.BDDMockito.given;

import com.team3.otboo.domain.feed.entity.FeedCommentCount;
import com.team3.otboo.domain.feed.entity.FeedLikeCount;
import com.team3.otboo.domain.feed.repository.FeedCommentCountRepository;
import com.team3.otboo.domain.feed.repository.FeedLikeCountRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.random.RandomGenerator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HotFeedScoreCalculatorTest {

	@InjectMocks
	HotFeedScoreCalculator hotFeedScoreCalculator;

	@Mock
	FeedLikeCountRepository feedLikeCountRepository;

	@Mock
	FeedCommentCountRepository feedCommentCountRepository;

	@Test
	void calculateTest() {
		UUID feedId = UUID.randomUUID();

		long likeCount = RandomGenerator.getDefault().nextLong(100);
		long commentCount = RandomGenerator.getDefault().nextLong(100);

		FeedLikeCount feedLikeCount = FeedLikeCount.init(feedId, likeCount);
		FeedCommentCount feedCommentCount = FeedCommentCount.init(feedId, commentCount);

		given(feedLikeCountRepository.findById(feedId))
			.willReturn(Optional.of(feedLikeCount));
		given(feedCommentCountRepository.findById(feedId))
			.willReturn(Optional.of(feedCommentCount));

		// when
		long score = hotFeedScoreCalculator.calculate(feedId);

		// then
		Assertions.assertThat(score)
			.isEqualTo(likeCount * 3 + commentCount);
	}
}
