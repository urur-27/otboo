package com.team3.otboo.domain.hot.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventType;
import com.team3.otboo.domain.hot.service.eventhandler.EventHandler;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HotFeedServiceTest {

	@InjectMocks
	HotFeedService hotFeedService;

	@Mock
	List<EventHandler> eventHandlers;

	@Mock
	HotFeedScoreUpdater hotFeedScoreUpdater;

	@Test
	@DisplayName("handler 가 없으면 handle 하지 않음 .")
	void handlerEventTest() {
		EventHandler eventHandler = mock(EventHandler.class);
		Event event = mock(Event.class); // 발생한 이벤트 .

		given(eventHandler.supports(event)).willReturn(false);
		given(eventHandlers.stream()).willReturn(Stream.of(eventHandler));

		// when
		hotFeedService.handleEvent(event);

		// then
		verify(eventHandler, never()).handle(event);
		verify(hotFeedScoreUpdater, never()).update(event, eventHandler);
	}

	@Test
	@DisplayName("Feed 에 대한 이벤트면 score 를 업데이트 하지 않음 .")
	void handlerEventTest2() {
		Event event = mock(Event.class); // 발생한 이벤트 ..
		EventHandler eventHandler = mock(EventHandler.class);

		given(event.getType()).willReturn(EventType.FEED_CREATED);
		given(eventHandler.supports(event)).willReturn(true);
		given(eventHandlers.stream()).willReturn(Stream.of(eventHandler));

		// when
		hotFeedService.handleEvent(event);

		// then
		verify(eventHandler).handle(event); // eventHandler 로 바로감 .
		verify(hotFeedScoreUpdater, never()).update(event, eventHandler);
	}

	@Test
	@DisplayName("feed delete 이벤트면 update x")
	void handlerEventTest3() {
		Event event = mock(Event.class);
		EventHandler eventHandler = mock(EventHandler.class); // feed delete event handler

		given(event.getType()).willReturn(EventType.FEED_DELETE);
		given(eventHandlers.stream()).willReturn(Stream.of(eventHandler));
		given(eventHandler.supports(event)).willReturn(true);

		// when
		hotFeedService.handleEvent(event);

		// then
		verify(eventHandler).handle(event);
		verify(hotFeedScoreUpdater, never()).update(event, eventHandler);
	}

	@Test
	@DisplayName("Feed 에 대한 이벤트가 아닌 다른 이벤트면, update 를 해야함")
	void handlerEventTest4() {
		Event event = mock(Event.class);
		EventHandler eventHandler = mock(EventHandler.class); // comment create handler

		given(event.getType()).willReturn(EventType.COMMENT_CREATED);
		given(eventHandlers.stream()).willReturn(Stream.of(eventHandler));
		given(eventHandler.supports(event)).willReturn(true);

		// when
		hotFeedService.handleEvent(event);

		// then
		verify(eventHandler, never()).handle(event);
		verify(hotFeedScoreUpdater).update(event, eventHandler);
	}
}
