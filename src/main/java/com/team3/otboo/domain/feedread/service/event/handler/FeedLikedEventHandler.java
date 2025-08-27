package com.team3.otboo.domain.feedread.service.event.handler;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventType;
import com.team3.otboo.common.event.payload.FeedLikedEventPayload;
import com.team3.otboo.domain.feedread.repository.FeedQueryModelRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Component;

@Component("feedReadFeedLikedEventHandler")
@RequiredArgsConstructor
@Slf4j
public class FeedLikedEventHandler implements EventHandler<FeedLikedEventPayload> {

	private final FeedQueryModelRepository feedQueryModelRepository;

	private final ElasticsearchOperations elasticsearchOperations;

	@Override
	public void handle(Event<FeedLikedEventPayload> event) {
		FeedLikedEventPayload payload = event.getPayload();
		// 여기서 동시성 문제가 발생
		feedQueryModelRepository.read(payload.getFeedId())
			.ifPresent(feedQueryModel -> {
				feedQueryModel.updateBy(payload);
				feedQueryModelRepository.update(feedQueryModel);

				Long likeCount = payload.getLikeCount();

				Map<String, Object> patch = Map.of("likeCount", likeCount);
				log.info("[FeedLikedEventHandler.handle] likeCount: {}", likeCount);
				UpdateQuery updateQuery = UpdateQuery.builder(feedQueryModel.getId().toString())
					.withDocument(Document.from(patch))
					.build();

				elasticsearchOperations.update(updateQuery, IndexCoordinates.of("feeds"));
			});
	}

	@Override
	public boolean supports(Event<FeedLikedEventPayload> event) {
		return event.getType() == EventType.FEED_LIKED;
	}
}
