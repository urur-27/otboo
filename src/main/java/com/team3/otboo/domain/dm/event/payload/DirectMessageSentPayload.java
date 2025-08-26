package com.team3.otboo.domain.dm.event.payload;

import com.team3.otboo.common.event.EventPayload;
import com.team3.otboo.domain.dm.dto.DirectMessageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectMessageSentPayload implements EventPayload {

	private String dmKey;
	private DirectMessageDto directMessageDto;
}
