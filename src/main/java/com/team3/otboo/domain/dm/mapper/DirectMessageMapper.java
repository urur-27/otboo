package com.team3.otboo.domain.dm.mapper;

import com.team3.otboo.domain.dm.dto.DirectMessageDto;
import com.team3.otboo.domain.dm.entity.DirectMessage;
import com.team3.otboo.domain.user.dto.UserSummary;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DirectMessageMapper {

	private final UserRepository userRepository;

	// n+1 문제 해결해야함 .
	public DirectMessageDto toDto(DirectMessage directMessage) {
		User sender = userRepository.findById(directMessage.getSenderId()).orElse(null);
		User receiver = userRepository.findById(directMessage.getReceiverId()).orElse(null);
		return new DirectMessageDto(
			directMessage.getId(),
			directMessage.getCreatedAt(),
			UserSummary.from(sender),
			UserSummary.from(receiver),
			directMessage.getContent()
		);
	}
}
