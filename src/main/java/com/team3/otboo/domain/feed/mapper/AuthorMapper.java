package com.team3.otboo.domain.feed.mapper;

import com.team3.otboo.domain.feed.dto.AuthorDto;
import com.team3.otboo.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AuthorMapper {

	public AuthorDto toDto(User user) {
		return new AuthorDto(
			user.getId(),
			user.getUsername(),
			user.getProfile().getBinaryContent().getImageUrl()
		);
	}
}
