package com.team3.otboo.domain.feed.service;

import com.team3.otboo.common.event.EventType;
import com.team3.otboo.common.event.payload.FeedLikedEventPayload;
import com.team3.otboo.common.event.payload.FeedUnlikedEventPayload;
import com.team3.otboo.common.outboxMessageRelay.OutboxEventPublisher;
import com.team3.otboo.domain.feed.dto.FeedDto;
import com.team3.otboo.domain.feed.entity.Feed;
import com.team3.otboo.domain.feed.entity.FeedLikeCount;
import com.team3.otboo.domain.feed.entity.Like;
import com.team3.otboo.domain.feed.mapper.FeedDtoAssembler;
import com.team3.otboo.domain.feed.repository.FeedLikeCountRepository;
import com.team3.otboo.domain.feed.repository.FeedRepository;
import com.team3.otboo.domain.feed.repository.LikeRepository;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.event.FeedLikedEvent;
import com.team3.otboo.global.exception.user.UserNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService {

	private final LikeRepository likeRepository;
	private final FeedLikeCountRepository feedLikeCountRepository;
	private final UserRepository userRepository;
	private final FeedRepository feedRepository;
	private final FeedDtoAssembler feedDtoAssembler;

	private final ApplicationEventPublisher eventPublisher;
	private final OutboxEventPublisher outboxEventPublisher;

	@Transactional
	public FeedDto like(UUID userId, UUID feedId) {
		if (likeRepository.existsByUserIdAndFeedId(userId, feedId)) {
			return feedDtoAssembler.assemble(feedId, userId);
		}

		User liker = userRepository.findById(userId)
			.orElseThrow(UserNotFoundException::new);
		Feed feed = feedRepository.findById(feedId)
			.orElseThrow(UserNotFoundException::new);
		User feedOwner = userRepository.findById(feed.getAuthorId())
			.orElseThrow(UserNotFoundException::new);

		Like savedLike = likeRepository.save(
			Like.create(
				feedId,
				userId)
		);

		int result = feedLikeCountRepository.increase(feedId);
		if (result == 0) {
			feedLikeCountRepository.save(FeedLikeCount.init(feedId, 1L));
		}

		eventPublisher.publishEvent(new FeedLikedEvent(feedOwner, liker.getUsername(), feedId));

		outboxEventPublisher.publish(
			EventType.FEED_LIKED,
			FeedLikedEventPayload.builder()
				.id(savedLike.getId())
				.createdAt(savedLike.getCreatedAt())
				.updatedAt(savedLike.getUpdatedAt())
				.feedId(savedLike.getFeedId())
				.userId(savedLike.getUserId())
				.likeCount(count(feedId))
				.build()
		);

		// likeCount 를 바로 사용하지 못하고 DB에 저장했다가 저장한걸 꺼내는 식으로 구현 되어 있음.
		// assemble 메서드의 파라미터로 likeCount 를 넣어주면 진짜 조금이라도 더 성능이 좋아지지 않을까 .
		// 좋아요 누르면 FeedDto 를 반환 .. FeedDto 를 만드는게 생각보다 무거운 작업인데 FeedDto 를 반환해야할까 ? 이 부분 개선해도 좋을 것 같음 .
		return feedDtoAssembler.assemble(feedId, userId);
	}

	@Transactional
	public void unlike(UUID userId, UUID feedId) {
		likeRepository.findByUserIdAndFeedId(userId, feedId)
			.ifPresent(feedLike -> {
				likeRepository.delete(feedLike);
				feedLikeCountRepository.decrease(feedId);
				outboxEventPublisher.publish(
					EventType.FEED_UNLIKED,
					FeedUnlikedEventPayload.builder()
						.id(feedLike.getId())
						.createdAt(feedLike.getCreatedAt())
						.updatedAt(feedLike.getUpdatedAt())
						.feedId(feedLike.getFeedId())
						.userId(feedLike.getUserId())
						.likeCount(count(feedId))
						.build()
				);
			});

		log.info("[LikeService.unlike] count(feedLike.getFeedId(): " + count(feedId));
	}

	@Transactional
	public void deleteAllByFeedId(UUID feedId) {
		likeRepository.deleteAllByFeedId(feedId);
	}

	public Long count(UUID feedId) {
		return feedLikeCountRepository.findById(feedId)
			.map(FeedLikeCount::getLikeCount)
			.orElse(0L);
	}
}
