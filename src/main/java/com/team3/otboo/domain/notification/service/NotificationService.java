package com.team3.otboo.domain.notification.service;

import com.team3.otboo.domain.notification.dto.NotificationDto;
import com.team3.otboo.domain.notification.dto.NotificationDtoCursorResponse;
import com.team3.otboo.domain.notification.dto.NotificationSearchCondition;
import com.team3.otboo.domain.notification.entity.Notification;
import com.team3.otboo.domain.notification.mapper.NotificationMapper;
import com.team3.otboo.domain.notification.repository.NotificationRepository;
import com.team3.otboo.domain.notification.service.strategy.NotificationStrategy;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.global.exception.notification.NotificationNotFoundException;
import com.team3.otboo.global.exception.user.UserNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final RedisTemplate<String, Object> redisTemplate;
	private final NotificationMapper notificationMapper;
	private final List<NotificationStrategy<?>> strategies;
	private final UserRepository userRepository;

	private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of("createdAt", "title");

	// 👇 2. 생성자를 직접 작성하고, 파라미터에 @Qualifier를 붙여줍니다.
	public NotificationService(
		NotificationRepository notificationRepository,
		@Qualifier("chatPubSub") RedisTemplate<String, Object> redisTemplate,
		NotificationMapper notificationMapper,
		List<NotificationStrategy<?>> strategies,
		UserRepository userRepository
	) {
		this.notificationRepository = notificationRepository;
		this.redisTemplate = redisTemplate;
		this.notificationMapper = notificationMapper;
		this.strategies = strategies;
		this.userRepository = userRepository;
	}

	@EventListener
	@Transactional
	public void handleEvent(Object event) {
		strategies.stream()
			.filter(strategy -> strategy.supports(event))
			.findFirst()
			.ifPresent(strategy -> processNotification(strategy, event));
	}

	@SuppressWarnings("unchecked")
	private <E> void processNotification(NotificationStrategy<E> strategy, Object event) {
		List<Notification> notifications = strategy.createNotification((E) event);

		for (Notification notification : notifications) {
			notificationRepository.save(notification);
			NotificationDto dto = notificationMapper.toDto(notification);
			redisTemplate.convertAndSend("notification-channel", dto);
		}
	}

	@Transactional(readOnly = true)
	public NotificationDtoCursorResponse findNotificationsByUserId(UUID userId,
		NotificationSearchCondition condition) {
		if (!ALLOWED_SORT_PROPERTIES.contains(condition.sortBy())) {
			throw new IllegalArgumentException("Invalid sort property: " + condition.sortBy());
		}

		User user = userRepository.findById(userId)
			.orElseThrow(UserNotFoundException::new);

		List<Notification> notifications = notificationRepository.findByReceiverWithCursor(
			user,
			condition
		);

		boolean hasNext = notifications.size() > condition.limit();
		List<Notification> currentPage =
			hasNext ? notifications.subList(0, condition.limit()) : notifications;

		List<NotificationDto> dtoList = notificationMapper.toDtoList(currentPage);

		String nextCursorValue = null;
		UUID nextIdAfterValue = null;
		if (hasNext) {
			Notification lastNotification = currentPage.getLast();
			if ("title".equals(condition.sortBy())) {
				nextCursorValue = lastNotification.getTitle();
			} else {
				nextCursorValue = lastNotification.getCreatedAt().toString();
			}
			nextIdAfterValue = lastNotification.getId();
		}

		return new NotificationDtoCursorResponse(
			dtoList,
			nextCursorValue,
			nextIdAfterValue,
			hasNext,
			notificationRepository.countByReceiver(user),
			condition.sortBy(),
			condition.sortDirection()
		);
	}

	@Transactional
  public void readNotification(UUID notificationId, UUID userId) {
		Notification notification = notificationRepository.findById(notificationId).orElseThrow(NotificationNotFoundException::new);

		if (!notification.getReceiver().getId().equals(userId)) {
			throw new AccessDeniedException("알림을 처리할 권한이 없습니다.");
		}

		notification.read();
  }
}
