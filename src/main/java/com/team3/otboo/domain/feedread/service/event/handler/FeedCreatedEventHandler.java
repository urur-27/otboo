package com.team3.otboo.domain.feedread.service.event.handler;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventType;
import com.team3.otboo.common.event.payload.FeedCreatedEventPayload;
import com.team3.otboo.domain.feed.dto.FeedDto;
import com.team3.otboo.domain.feed.mapper.FeedDtoAssembler;
import com.team3.otboo.domain.feedread.document.FeedDocument;
import com.team3.otboo.domain.feedread.repository.FeedIdListRepository;
import com.team3.otboo.domain.feedread.repository.FeedQueryModel;
import com.team3.otboo.domain.feedread.repository.FeedQueryModelRepository;
import com.team3.otboo.domain.feedread.repository.FeedSearchRepository;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("feedReadFeedCreatedEventHandler")
@RequiredArgsConstructor
public class FeedCreatedEventHandler implements EventHandler<FeedCreatedEventPayload> {

	private final FeedQueryModelRepository feedQueryModelRepository;
	private final FeedIdListRepository feedIdListRepository;

	private final FeedDtoAssembler feedDtoAssembler;
	private final FeedSearchRepository feedSearchRepository;

	@Override
	public void handle(Event<FeedCreatedEventPayload> event) {
		FeedCreatedEventPayload payload = event.getPayload();
		// payload 받아서 AuthorDto 나 WeatherDto 를 feedDtoAssembler 로 조립함 .
		UUID feedId = payload.getId();
		FeedDto feedDto = feedDtoAssembler.assemble(feedId);
		feedQueryModelRepository.create(
			FeedQueryModel.create(feedDto),
			Duration.ofDays(1)
		);
		feedIdListRepository.add(feedId, payload.getCreatedAt(), 1000L); // id list 는 1000개 까지 저장 .

		FeedDocument feedDocument = new FeedDocument();
		feedDocument.setId(payload.getId());
		feedDocument.setCreatedAt(payload.getCreatedAt());
		feedDocument.setContent(payload.getContent());
		feedDocument.setAuthorId(payload.getAuthorId());
		feedDocument.setAuthorName(feedDto.author().name());
		feedDocument.setSkyStatus(payload.getSkyStatus());
		feedDocument.setPrecipitationType(payload.getPrecipitationType());
		feedDocument.setLikeCount(0); // 생성시 like count 는 0
		feedSearchRepository.save(feedDocument);
	}

	@Override
	public boolean supports(Event<FeedCreatedEventPayload> event) {
		return event.getType() == EventType.FEED_CREATED;
	}
}
