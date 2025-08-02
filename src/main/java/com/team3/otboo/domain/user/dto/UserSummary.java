package com.team3.otboo.domain.user.dto;

import com.team3.otboo.domain.user.entity.User;
import java.util.UUID;

public record UserSummary(
	UUID userId,
	String name,
	String profileImageUrl
) {

	public static UserSummary from(User user) {
		return new UserSummary(
			user.getId(),
			user.getUsername(),
			user.getProfile().getBinaryContent().getImageUrl()
		);
	}
}
