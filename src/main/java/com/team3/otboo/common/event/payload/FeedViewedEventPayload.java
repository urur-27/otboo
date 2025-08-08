package com.team3.otboo.common.event.payload;

import com.team3.otboo.common.event.EventPayload;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedViewedEventPayload implements EventPayload {

	private UUID feedId;
	private Long feedViewCount;
}
