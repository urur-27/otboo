package com.team3.otboo.common.event.payload;

import com.team3.otboo.common.event.EventPayload;
import com.team3.otboo.domain.weather.enums.PrecipitationType;
import com.team3.otboo.domain.weather.enums.SkyStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedCreatedEventPayload implements EventPayload {

	private UUID id; // feedId
	private Instant createdAt;
	private Instant updatedAt;
	private UUID authorId;
	private UUID weatherId;
	private String content;

	private SkyStatus skyStatus;
	private PrecipitationType precipitationType;
}
