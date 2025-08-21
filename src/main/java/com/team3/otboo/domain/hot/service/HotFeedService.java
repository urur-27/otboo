package com.team3.otboo.domain.hot.service;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventPayload;
import com.team3.otboo.common.event.EventType;
import com.team3.otboo.domain.feed.dto.FeedDto;
import com.team3.otboo.domain.feed.mapper.FeedDtoAssembler;
import com.team3.otboo.domain.feedread.client.FeedClient;
import com.team3.otboo.domain.hot.repository.HotFeedListRepository;
import com.team3.otboo.domain.hot.service.event.handler.EventHandler;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotFeedService {

	private final FeedClient feedClient; // 원본 feed 를 조회해옴 . (hotFeedListRepository 는 id만 저장)
	private final List<EventHandler<EventPayload>> eventHandlers;
	private final HotFeedScoreUpdater hotFeedScoreUpdater;
	private final HotFeedListRepository hotFeedListRepository;
	private final FeedDtoAssembler feedDtoAssembler;

	// event 를 카프카 통해서 전달 받음 .
	public void handleEvent(Event<EventPayload> event) {
		EventHandler<EventPayload> eventHandler = findEventHandler(event);
		if (eventHandler == null) {
			log.warn("handler not found for eventType: {}", event.getType());
			return; // 해당 이벤트를 처리할 핸들러 없으면 아무것도 안함.
		}

		if (isFeedCreatedOrDeleted(event)) { // feed 생성 이벤트면
			eventHandler.handle(event);
		} else {
			hotFeedScoreUpdater.update(event, eventHandler);
		}
	}

	private EventHandler<EventPayload> findEventHandler(Event<EventPayload> event) {
		return eventHandlers.stream()
			.filter(eventHandler -> eventHandler.supports(event))
			.findFirst()
			.orElse(null);
	}

	private boolean isFeedCreatedOrDeleted(Event<EventPayload> event) {
		return event.getType() == EventType.FEED_CREATED
			|| event.getType() == EventType.FEED_DELETE;
	}

	// id 를 반환하는 메서드
	public List<String> readAll(String dateStr) {
		return hotFeedListRepository.readAll(dateStr);
	}

	// FeedDto 를 반환하는 메서드 .. 나중에 인기 피드를 따로 만들어서 인기 피드만 보여줄때 사용 userId -> 현재 로그인한 유저
	public List<FeedDto> readAllFeeds(String dateStr, UUID userId) {
		// n이 최대 10개인데 N+1 문제를 해결해야할까
		return hotFeedListRepository.readAll(dateStr).stream()
			.map(feedId -> feedDtoAssembler.assemble(UUID.fromString(feedId), userId))
			.toList();
	}
}
