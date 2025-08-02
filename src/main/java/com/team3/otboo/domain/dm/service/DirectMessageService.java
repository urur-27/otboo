package com.team3.otboo.domain.dm.service;

import com.team3.otboo.domain.dm.dto.DirectMessageSendPayload;
import com.team3.otboo.domain.dm.entity.DirectMessage;
import com.team3.otboo.domain.dm.mapper.DirectMessageMapper;
import com.team3.otboo.domain.dm.repository.DirectMessageRepository;
import com.team3.otboo.domain.dm.service.request.DirectMessageCreateRequest;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DirectMessageService {

	private final DirectMessageRepository directMessageRepository;
	private DirectMessageMapper directMessageMapper;

	public DirectMessageSendPayload save(DirectMessageCreateRequest request) {
		String dmKey = createDmKey(request.senderId(), request.receiverId());

		DirectMessage directMessage = directMessageRepository.save(
			DirectMessage.create(
				request.senderId(),
				request.receiverId(),
				request.content()
			)
		);
		return new DirectMessageSendPayload(dmKey, directMessageMapper.toDto(directMessage));
	}

	// dm 목록 조회 .

	private String createDmKey(UUID senderId, UUID receiverId) {
		List<UUID> userIds = Arrays.asList(senderId, receiverId);
		userIds.sort(Comparator.comparing(UUID::toString));
		return userIds.get(0) + "_" + userIds.get(1);
	}
}
