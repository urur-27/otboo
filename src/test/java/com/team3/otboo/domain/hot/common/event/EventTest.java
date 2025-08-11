package com.team3.otboo.domain.hot.common.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventPayload;
import com.team3.otboo.common.event.EventType;
import com.team3.otboo.common.event.payload.FeedCreatedEventPayload;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class EventTest {

	@Test
	void serde() {
		// 이벤트가 json 으로 왔을때 . Event 객체로 바꾸는 test

		FeedCreatedEventPayload payload = FeedCreatedEventPayload.builder()
			.id(UUID.randomUUID())
			.createdAt(Instant.now())
			.updatedAt(Instant.now())
			.authorId(UUID.randomUUID())
			.weatherId(UUID.randomUUID())
			.content("content")
			.build();

		Event<EventPayload> event = Event.of(
			UUID.randomUUID(),
			EventType.FEED_CREATED,
			payload
		);

		String json = event.toJson();
		System.out.println("json = " + json);

		// when
		Event<EventPayload> result = Event.fromJson(json);

		// then
		assertThat(result.getEventId()).isEqualTo(event.getEventId());
		assertThat(result.getType()).isEqualTo(EventType.FEED_CREATED);
		assertThat(result.getPayload()).isInstanceOf(payload.getClass());

		FeedCreatedEventPayload resultPayload = (FeedCreatedEventPayload) result.getPayload();
		assertThat(resultPayload.getId()).isEqualTo(payload.getId());
		assertThat(resultPayload.getContent()).isEqualTo(payload.getContent());
		assertThat(resultPayload.getCreatedAt()).isEqualTo(payload.getCreatedAt());
		assertThat(resultPayload.getAuthorId()).isEqualTo(payload.getAuthorId());
		assertThat(resultPayload.getWeatherId()).isEqualTo(payload.getWeatherId());
	}
}
