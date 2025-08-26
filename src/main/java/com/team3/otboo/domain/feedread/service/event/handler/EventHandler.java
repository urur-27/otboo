package com.team3.otboo.domain.feedread.service.event.handler;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventPayload;

public interface EventHandler<T extends EventPayload> {

	void handle(Event<T> event);

	boolean supports(Event<T> event);
}
