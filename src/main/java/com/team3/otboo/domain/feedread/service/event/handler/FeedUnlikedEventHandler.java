package com.team3.otboo.domain.feedread.service.event.handler;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventType;
import com.team3.otboo.common.event.payload.FeedUnlikedEventPayload;
import com.team3.otboo.domain.feedread.repository.FeedQueryModelRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Component;

@Component("feedReadFeedUnlikedEventHandler")
@RequiredArgsConstructor
@Slf4j
public class FeedUnlikedEventHandler implements EventHandler<FeedUnlikedEventPayload> {

	private final FeedQueryModelRepository feedQueryModelRepository;

	private final ElasticsearchOperations elasticsearchOperations;

	@Override
	public void handle(Event<FeedUnlikedEventPayload> event) {
		FeedUnlikedEventPayload payload = event.getPayload();
		feedQueryModelRepository.read(payload.getFeedId())
			.ifPresent(feedQueryModel -> {
				feedQueryModel.updateBy(payload);
				feedQueryModelRepository.update(feedQueryModel);

				Long likeCount = payload.getLikeCount();
				log.info("[FeedUnlikedEventHandler.handle] likeCount: {}", likeCount);

				Map<String, Object> patch = Map.of("likeCount", likeCount);
				UpdateQuery updateQuery = UpdateQuery.builder(feedQueryModel.getId().toString())
					.withDocument(Document.from(patch))
					.build();

				elasticsearchOperations.update(updateQuery, IndexCoordinates.of("feeds"));
			});
	}

	@Override
	public boolean supports(Event<FeedUnlikedEventPayload> event) {
		return event.getType() == EventType.FEED_UNLIKED;
	}
}
