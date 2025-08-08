package com.team3.otboo.common.event.payload;

import com.team3.otboo.common.event.EventPayload;
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
public class FeedDeletedEventPayload implements EventPayload {

	private UUID id;
	private Instant createdAt;
	private Instant updatedAt;
	private UUID authorId;
	private UUID weatherId;
	private String content;
}
