package com.team3.otboo.domain.dm.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DirectMessageDto(
	UUID id,
	LocalDateTime createdAt,
	UserSummary sender,
	UserSummary receiver,
	String content
) {

}
