package com.team3.otboo.domain.feedread.service.event.handler;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventType;
import com.team3.otboo.common.event.payload.FeedUpdatedEventPayload;
import com.team3.otboo.domain.feedread.document.FeedDocument;
import com.team3.otboo.domain.feedread.repository.FeedQueryModelRepository;
import com.team3.otboo.domain.feedread.repository.FeedSearchRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Component;

@Component("feedReadFeedUpdatedEventHandler")
@RequiredArgsConstructor
public class FeedUpdatedEventHandler implements EventHandler<FeedUpdatedEventPayload> {

	private final FeedQueryModelRepository feedQueryModelRepository;

	private final FeedSearchRepository feedSearchRepository;
	private final ElasticsearchOperations elasticsearchOperations;

	@Override
	public void handle(Event<FeedUpdatedEventPayload> event) {
		FeedUpdatedEventPayload payload = event.getPayload();
		feedQueryModelRepository.read(payload.getId())
			.ifPresent(feedQueryModel -> {
				feedQueryModel.updateBy(payload);
				feedQueryModelRepository.update(feedQueryModel);

			});

		FeedDocument feedDocument = feedSearchRepository.findById(payload.getId()).orElseThrow(() ->
			new EntityNotFoundException(
				"[FeedUpdatedEventHandler.handle] feed document not found. feed id: "
					+ payload.getId())
		);

		Map<String, Object> patch = Map.of("content", payload.getContent());
		UpdateQuery uq = UpdateQuery.builder(payload.getId().toString())
			.withDocument(org.springframework.data.elasticsearch.core.document.Document.from(patch))
			.build();
		elasticsearchOperations.update(uq, IndexCoordinates.of("feeds"));
	}

	@Override
	public boolean supports(Event<FeedUpdatedEventPayload> event) {
		return event.getType() == EventType.FEED_UPDATED;
	}
}
