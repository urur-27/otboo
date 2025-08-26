package com.team3.otboo.domain.hot.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.domain.feed.entity.Feed;
import com.team3.otboo.domain.feed.service.FeedService;
import com.team3.otboo.domain.hot.repository.HotFeedListRepository;
import com.team3.otboo.domain.hot.service.event.handler.EventHandler;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HotFeedScoreUpdaterTest {

	@InjectMocks
	HotFeedScoreUpdater hotFeedScoreUpdater;
	@Mock
	HotFeedListRepository hotFeedListRepository;
	@Mock
	HotFeedScoreCalculator hotFeedScoreCalculator;
	@Mock
	FeedService feedService;

	@Test
	@DisplayName("오늘 생성된 게시물이면, score 를 update 한다.")
	void updateIfFeedNotCreatedTodayTest() {

		// given
		UUID feedId = UUID.randomUUID();

		Feed feed = mock(Feed.class);
		Event event = mock(Event.class);
		EventHandler eventHandler = mock(EventHandler.class);

		given(feedService.read(feedId)).willReturn(feed);
		given(feed.getCreatedAt()).willReturn(Instant.now());
		given(eventHandler.findFeedId(event)).willReturn(feedId);

		// when
		hotFeedScoreUpdater.update(event, eventHandler);

		// then
		verify(eventHandler).handle(event);
		verify(hotFeedListRepository)
			.add(any(UUID.class),
				any(LocalDateTime.class),
				anyLong(),
				anyLong(),
				any(Duration.class
				)
			);
	}

	@Test
	@DisplayName("오늘 생성된 게시물이 아니라면, 정보가 업데이트 되지않음 .")
	void updateTest() {
		UUID feedId = UUID.randomUUID();

		Event event = mock(Event.class);
		EventHandler eventHandler = mock(EventHandler.class);
		Feed feed = mock(Feed.class);

		given(feedService.read(feedId)).willReturn(feed);
		given(feed.getCreatedAt()).willReturn(Instant.now().minus(1, ChronoUnit.DAYS));
		given(eventHandler.findFeedId(event)).willReturn(feedId);

		// when
		hotFeedScoreUpdater.update(event, eventHandler);

		// then
		verify(eventHandler, never()).handle(event);
		verify(hotFeedListRepository, never()).add(any(), any(), any(), any(), any());
	}
}
