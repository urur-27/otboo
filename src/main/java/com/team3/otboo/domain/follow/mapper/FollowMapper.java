package com.team3.otboo.domain.follow.mapper;

import com.team3.otboo.domain.follow.dto.FollowDto;
import com.team3.otboo.domain.follow.entity.Follow;
import com.team3.otboo.domain.user.dto.UserSummary;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FollowMapper {

	private final UserRepository userRepository;

	public FollowDto toDto(Follow follow) {

		User followee = userRepository.findById(follow.getFolloweeId())
			.orElseThrow(() -> new EntityNotFoundException("user not found"));
		User follower = userRepository.findById(follow.getFollowerId())
			.orElseThrow(() -> new EntityNotFoundException("user not found"));

		return new FollowDto(
			follow.getId(),
			new UserSummary(followee.getId(), followee.getUsername(),
				followee.getProfile().getBinaryContent().getImageUrl()),
			new UserSummary(follower.getId(), follower.getUsername(),
				follower.getProfile().getBinaryContent().getImageUrl())
		);
	}
}
