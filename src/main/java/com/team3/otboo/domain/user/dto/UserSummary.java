package com.team3.otboo.domain.user.dto;

import com.team3.otboo.domain.user.entity.Profile;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.storage.entity.BinaryContent;
import java.util.Optional;
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
			Optional.ofNullable(user.getProfile())
				.map(Profile::getBinaryContent)
				.map(BinaryContent::getImageUrl)
				.orElse(null)
		);
	}
}
