package com.team3.otboo.common.event;

import com.team3.otboo.common.event.payload.CommentCreatedEventPayload;
import com.team3.otboo.common.event.payload.FeedCreatedEventPayload;
import com.team3.otboo.common.event.payload.FeedDeletedEventPayload;
import com.team3.otboo.common.event.payload.FeedLikedEventPayload;
import com.team3.otboo.common.event.payload.FeedUnlikedEventPayload;
import com.team3.otboo.common.event.payload.FeedUpdatedEventPayload;
import com.team3.otboo.common.event.payload.FeedViewedEventPayload;
import com.team3.otboo.domain.dm.event.payload.DirectMessageSentPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum EventType {
	FEED_CREATED(FeedCreatedEventPayload.class, Topic.OTBOO_FEED),
	FEED_UPDATED(FeedUpdatedEventPayload.class, Topic.OTBOO_FEED),
	FEED_DELETE(FeedDeletedEventPayload.class, Topic.OTBOO_FEED),
	COMMENT_CREATED(CommentCreatedEventPayload.class, Topic.OTBOO_FEED_COMMENT),
	FEED_LIKED(FeedLikedEventPayload.class, Topic.OTBOO_FEED_LIKE),
	FEED_UNLIKED(FeedUnlikedEventPayload.class, Topic.OTBOO_FEED_LIKE),
	DIRECT_MESSAGE_SENT(DirectMessageSentPayload.class, Topic.OTBOO_DIRECT_MESSAGE),
	FEED_VIEWED(FeedViewedEventPayload.class, Topic.OTBOO_FEED_VIEW);

	private final Class<? extends EventPayload> payloadClass;
	private final String topic;

	// raw 데이터에 있는 타입 받아서 enum type 으로 변환해줌 .
	public static EventType from(String type) {
		try {
			return valueOf(type); // 문자열을 받아서 enum 상수를 반환함 . "FEED_CREATE" -> EventType.FEED_CREATE
		} catch (Exception e) {
			log.error("[EventType.from] type={}", type, e);
			return null;
		}
	}

	public static class Topic {

		public static final String OTBOO_FEED = "otboo-feed";
		public static final String OTBOO_FEED_COMMENT = "otboo-feed-comment";
		public static final String OTBOO_FEED_LIKE = "otboo-feed-like";
		public static final String OTBOO_FEED_VIEW = "otboo-feed-view";
		public static final String OTBOO_DIRECT_MESSAGE = "otboo-message-direct";
	}
}
