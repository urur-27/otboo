package com.team3.otboo.domain.hot.service.event.handler;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventPayload;
import java.util.UUID;

public interface EventHandler<T extends EventPayload> {

	void handle(Event<T> event);

	boolean supports(Event<T> event); // 이 핸들러가 이벤트를 지원하는지 확인 .

	UUID findFeedId(Event<T> event);
}
