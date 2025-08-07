package com.team3.otboo.domain.dm.service;

import com.team3.otboo.domain.dm.dto.DirectMessageDto;
import com.team3.otboo.domain.dm.dto.DirectMessageDtoCursorResponse;
import com.team3.otboo.domain.dm.dto.DirectMessageSendPayload;
import com.team3.otboo.domain.dm.entity.DirectMessage;
import com.team3.otboo.domain.dm.entity.DirectMessageCount;
import com.team3.otboo.domain.dm.mapper.DirectMessageMapper;
import com.team3.otboo.domain.dm.repository.DirectMessageCountRepository;
import com.team3.otboo.domain.dm.repository.DirectMessageRepository;
import com.team3.otboo.domain.dm.service.request.DirectMessageCreateRequest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.SortDirection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DirectMessageService {

	private final DirectMessageRepository directMessageRepository;
	private final DirectMessageCountRepository directMessageCountRepository;

	private final DirectMessageMapper directMessageMapper;

	@Transactional
	public DirectMessageSendPayload save(DirectMessageCreateRequest request) {
		DirectMessage directMessage = directMessageRepository.save(
			DirectMessage.create(
				request.senderId(),
				request.receiverId(),
				request.content()
			)
		);

		String dmKey = createDmKey(request.senderId(), request.receiverId());
		int result = directMessageCountRepository.increase(dmKey);
		if (result == 0) {
			directMessageCountRepository.save(DirectMessageCount.init(dmKey, 1L));
		}

		return new DirectMessageSendPayload(dmKey, directMessageMapper.toDto(directMessage));
	}

	public DirectMessageDtoCursorResponse getDirectMessages(UUID userId, UUID currentUserId,
		String cursor, UUID idAfter, int limit) {

		// count 쿼리 vs directMessageCountRepository 따로 만들기
		// dm 은 쓰기 작업이 더 많을 거 같은데 count repository 만들면 쓸때마다 db 에 접근
		// DirectMessageCount 를 레디스에 저장할까 ? cursor 페이지네이션 요청마다 count 해야함 .
		String dmKey = createDmKey(userId, currentUserId);

		Long count = directMessageCountRepository.findById(dmKey)
			.map(DirectMessageCount::getDirectMessageCount)
			.orElse(0L);
		int totalCount = count.intValue();

		Instant lastCreatedAt = null;
		if (cursor != null) {
			lastCreatedAt = Instant.parse(cursor);
		}

		List<DirectMessage> messages = cursor == null || idAfter == null ?
			directMessageRepository.getDirectMessages(userId, currentUserId, limit + 1) :
			directMessageRepository.getDirectMessages(userId, currentUserId,
				lastCreatedAt, idAfter, limit + 1);

		boolean hasNext = messages.size() > limit;
		List<DirectMessage> currentPage = hasNext ? messages.subList(0, limit) : messages;

		List<DirectMessageDto> directMessageDtoList = currentPage.stream()
			.map(directMessageMapper::toDto)
			.toList();

		String nextCursor = null;
		UUID nextIdAfter = null;
		if (hasNext && !currentPage.isEmpty()) {
			DirectMessage lastElements = currentPage.getLast();
			nextCursor = lastElements.getCreatedAt().toString();
			nextIdAfter = lastElements.getId();
		}

		return new DirectMessageDtoCursorResponse(
			directMessageDtoList,
			nextCursor,
			nextIdAfter,
			hasNext,
			totalCount,
			"created_at, id",
			SortDirection.ASCENDING
		);
	}

	private String createDmKey(UUID userId1, UUID userId2) {
		return userId1.compareTo(userId2) < 0 ?
			userId1 + "_" + userId2 : userId2 + "_" + userId1;
	}
}
