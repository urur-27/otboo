package com.team3.otboo.common.event;

import com.team3.otboo.common.dataSerializer.DataSerializer;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Event<T extends EventPayload> {

	private UUID eventId;
	private EventType type;
	private T payload;

	public static Event<EventPayload> of(UUID eventId, EventType type, EventPayload payload) {
		Event<EventPayload> event = new Event<>();
		event.eventId = eventId;
		event.type = type;
		event.payload = payload;
		return event;
	}

	public String toJson() {
		return DataSerializer.serialize(this);
	}

	public static Event<EventPayload> fromJson(String json) {
		EventRaw eventRaw = DataSerializer.deserialize(json, EventRaw.class);
		if (eventRaw == null) {
			return null;
		}
		Event<EventPayload> event = new Event<>();
		event.eventId = eventRaw.getEventId();
		event.type = EventType.from(eventRaw.getType());
		event.payload = DataSerializer.deserialize(eventRaw.getPayload(),
			event.type.getPayloadClass());
		return event;
	}

	@Getter
	private static class EventRaw {

		private UUID eventId;
		private String type;
		private Object payload;
	}
}
