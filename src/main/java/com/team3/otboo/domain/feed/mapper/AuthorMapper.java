package com.team3.otboo.domain.feed.mapper;

import com.team3.otboo.domain.feed.dto.AuthorDto;
import com.team3.otboo.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AuthorMapper {

	public AuthorDto toDto(User user) {
		String imageUrl = null;
		if (user.getProfile().getBinaryContent() != null) {
			imageUrl = user.getProfile().getBinaryContent().getImageUrl();
		}
		return new AuthorDto(
			user.getId(),
			user.getUsername(),
			imageUrl
		);
	}
}
