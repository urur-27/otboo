package com.team3.otboo.domain.dm.dto;

import com.team3.otboo.domain.user.dto.UserSummary;
import java.time.Instant;
import java.util.UUID;

public record DirectMessageDto(
	UUID id,
	Instant createdAt,
	UserSummary sender,
	UserSummary receiver,
	String content
) {

}
