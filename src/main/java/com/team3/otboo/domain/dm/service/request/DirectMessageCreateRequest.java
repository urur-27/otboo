package com.team3.otboo.domain.dm.service.request;

import java.util.UUID;

public record DirectMessageCreateRequest(
	UUID receiverId,
	UUID senderId,
	String content
) {
}
