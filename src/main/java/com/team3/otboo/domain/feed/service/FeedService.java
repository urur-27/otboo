package com.team3.otboo.domain.feed.service;

import com.team3.otboo.common.event.EventType;
import com.team3.otboo.common.event.payload.FeedCreatedEventPayload;
import com.team3.otboo.common.event.payload.FeedDeletedEventPayload;
import com.team3.otboo.common.event.payload.FeedUpdatedEventPayload;
import com.team3.otboo.common.outboxMessageRelay.OutboxEventPublisher;
import com.team3.otboo.domain.feed.dto.FeedDto;
import com.team3.otboo.domain.feed.entity.Feed;
import com.team3.otboo.domain.feed.entity.FeedCommentCount;
import com.team3.otboo.domain.feed.entity.FeedCount;
import com.team3.otboo.domain.feed.entity.FeedLikeCount;
import com.team3.otboo.domain.feed.mapper.FeedDtoAssembler;
import com.team3.otboo.domain.feed.repository.FeedCommentCountRepository;
import com.team3.otboo.domain.feed.repository.FeedCountRepository;
import com.team3.otboo.domain.feed.repository.FeedLikeCountRepository;
import com.team3.otboo.domain.feed.repository.FeedRepository;
import com.team3.otboo.domain.feed.repository.FeedRepositoryQueryDSL;
import com.team3.otboo.domain.feed.repository.FeedViewCountBackUpRepository;
import com.team3.otboo.domain.feed.service.request.FeedCreateRequest;
import com.team3.otboo.domain.feed.service.request.FeedListRequest;
import com.team3.otboo.domain.feed.service.request.FeedUpdateRequest;
import com.team3.otboo.domain.feed.service.response.FeedDtoCursorResponse;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.event.FollowedUserPostedFeedEvent;
import com.team3.otboo.global.exception.user.UserNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedService {

	private final FeedRepository feedRepository;
	private final FeedDtoAssembler feedDtoAssembler;
	private final ApplicationEventPublisher eventPublisher;

	private final OotdService ootdService;
	private final CommentService commentService;
	private final LikeService likeService;

	private final FeedLikeCountRepository feedLikeCountRepository;
	private final FeedRepositoryQueryDSL feedRepositoryQueryDSL;
	private final UserRepository userRepository;
	private final FeedCommentCountRepository feedCommentCountRepository;
	private final FeedCountRepository feedCountRepository;
	private final FeedViewCountBackUpRepository feedViewCountBackUpRepository;

	private final OutboxEventPublisher outboxEventPublisher;

	// 알림 기능과 겹치니까 publish 는 한번만 하고, Listener 를 두개 써야함 .
	// 겹치는거 -> 좋아요 생성 삭제, 댓글 생성 시 알림 가야하고 + 인기 피드 쪽에서 점수 계산까지 해야함 .
	@Transactional
	public FeedDto create(UUID userId, FeedCreateRequest request) {

		User author = userRepository.findById(request.authorId())
			.orElseThrow(UserNotFoundException::new);

		Feed feed = feedRepository.save(Feed.create(
			request.authorId(),
			request.weatherId(),
			request.content())
		);

		// 동시성 문제를 피하기 위해 미리 초기화 해두기 .
		feedLikeCountRepository.save(FeedLikeCount.init(feed.getId(), 0L));
		feedCommentCountRepository.save(FeedCommentCount.init(feed.getId(), 0L));

		ootdService.create(feed.getId(), request.clothesIds());

		eventPublisher.publishEvent(new FollowedUserPostedFeedEvent(author, feed.getId()));

		int result = feedCountRepository.increase(FeedCount.SINGLETON_ID);
		if (result == 0) {
			feedCountRepository.save(FeedCount.init(1L));
		}

		log.info("[FeedService.create] feed created. feedId: " + feed.getId());
		outboxEventPublisher.publish(
			EventType.FEED_CREATED,
			FeedCreatedEventPayload.builder()
				.id(feed.getId())
				.createdAt(feed.getCreatedAt())
				.updatedAt(feed.getUpdatedAt())
				.authorId(feed.getAuthorId())
				.weatherId(feed.getWeatherId())
				.content(feed.getContent())
				.build()
		);

		return feedDtoAssembler.assemble(feed.getId(), userId);
	}

	@PreAuthorize("@feedSecurity.isAuthor(#feedId, authentication.principal.id)")
	@Transactional
	public FeedDto update(UUID feedId, UUID userId, FeedUpdateRequest request) {
		Feed feed = feedRepository.findById(feedId).orElseThrow(
			() -> new EntityNotFoundException("feed not found. feed id: " + feedId)
		);

		feed.update(request.content());

		outboxEventPublisher.publish(
			EventType.FEED_UPDATED,
			FeedUpdatedEventPayload.builder()
				.id(feed.getId())
				.updatedAt(feed.getUpdatedAt())
				.content(feed.getContent())
				.build()
		);

		return feedDtoAssembler.assemble(feedId, userId);
	}

	@PreAuthorize("@feedSecurity.isAuthor(#feedId, authentication.principal.id)")
	@Transactional
	public void delete(UUID feedId) {
		ootdService.deleteAllByFeedId(feedId);
		commentService.deleteAllByFeedId(feedId);
		likeService.deleteAllByFeedId(feedId);

		Feed feed = feedRepository.findById(feedId).orElseThrow(
			() -> new EntityNotFoundException("feed not found. feed id: " + feedId)
		);
		feedRepository.delete(feed);

		feedCommentCountRepository.deleteById(feedId);
		feedLikeCountRepository.deleteById(feedId);

		feedRepository.deleteById(feedId);
		feedCountRepository.decrease(FeedCount.SINGLETON_ID);

		outboxEventPublisher.publish(
			EventType.FEED_DELETE,
			FeedDeletedEventPayload.builder()
				.id(feed.getId())
				.createdAt(feed.getCreatedAt())
				.updatedAt(feed.getUpdatedAt())
				.authorId(feed.getAuthorId())
				.weatherId(feed.getWeatherId())
				.content(feed.getContent())
				.build()
		);
	}

	@Transactional(readOnly = true)
	public FeedDtoCursorResponse readAllInfiniteScroll(UUID userId, FeedListRequest request) {

		List<Feed> feeds = feedRepositoryQueryDSL.readAll(request);
		int totalCount = feedRepositoryQueryDSL.countFeeds(request);

		boolean hasNext = feeds.size() > request.limit();
		List<Feed> currentPage = hasNext ? feeds.subList(0, request.limit()) : feeds;

		List<FeedDto> data = currentPage.stream()
			.map(feed -> feedDtoAssembler.assemble(feed.getId(), userId))
			.toList();

		String nextCursor = null;
		UUID nextIdAfter = null;

		if (hasNext) {
			FeedDto lastElement = data.getLast();

			if (request.sortBy().equals("createdAt")) {
				nextCursor = lastElement.createdAt().toString();
			} else if (request.sortBy().equals("likeCount")) {
				nextCursor = lastElement.likeCount().toString();
			}

			nextIdAfter = lastElement.id();
		}

		return new FeedDtoCursorResponse(
			data,
			nextCursor,
			nextIdAfter,
			hasNext,
			totalCount,
			request.sortBy(),
			request.sortDirection()
		);
	}

	@Transactional(readOnly = true)
	public Feed read(UUID feedId) {
		return feedRepository.findById(feedId).orElseThrow(
			() -> new EntityNotFoundException("feed not found. feed id: " + feedId)
		);
	}

	@Transactional(readOnly = true)
	public FeedDto read(UUID feedId, UUID userId) {
		return feedDtoAssembler.assemble(feedId, userId);
	}

	@Transactional(readOnly = true)
	public Long count() {
		return feedCountRepository.findById(FeedCount.SINGLETON_ID)
			.map(FeedCount::getFeedCount)
			.orElse(0L);
	}
}
