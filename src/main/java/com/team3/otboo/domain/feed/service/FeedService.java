package com.team3.otboo.domain.feed.service;

import com.team3.otboo.domain.feed.dto.FeedDto;
import com.team3.otboo.domain.feed.dto.FeedDtoCursorResponse;
import com.team3.otboo.domain.feed.entity.Feed;
import com.team3.otboo.domain.feed.mapper.FeedDtoAssembler;
import com.team3.otboo.domain.feed.repository.FeedCommentCountRepository;
import com.team3.otboo.domain.feed.repository.FeedLikeCountRepository;
import com.team3.otboo.domain.feed.repository.FeedRepository;
import com.team3.otboo.domain.feed.repository.FeedRepositoryQueryDSL;
import com.team3.otboo.domain.feed.service.request.FeedCreateRequest;
import com.team3.otboo.domain.feed.service.request.FeedListRequest;
import com.team3.otboo.domain.feed.service.request.FeedUpdateRequest;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.event.FollowedUserPostedFeedEvent;
import com.team3.otboo.global.exception.user.UserNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedService {

	private final FeedRepository feedRepository;
	private final FeedDtoAssembler feedDtoAssembler;
	private final ApplicationEventPublisher eventPublisher;

	private final OotdService ootdService;
	private final CommentService commentService;
	private final FeedCommentCountRepository feedCommentCountRepository;
	private final LikeService likeService;
	private final FeedLikeCountRepository feedLikeCountRepository;
	private final FeedRepositoryQueryDSL feedRepositoryQueryDSL;
	private final UserRepository userRepository;

	@Transactional
	public FeedDto create(UUID userId, FeedCreateRequest request) {

		User author = userRepository.findById(request.authorId())
				.orElseThrow(UserNotFoundException::new);

		Feed feed = feedRepository.save(Feed.create(
			request.authorId(),
			request.weatherId(),
			request.content())
		);

		ootdService.create(feed.getId(), request.clothesIds());

		eventPublisher.publishEvent(new FollowedUserPostedFeedEvent(author, feed.getId()));

		return feedDtoAssembler.assemble(feed.getId(), userId);
	}

	//	TODO : @PreAuthorize()
	@Transactional
	public FeedDto update(UUID feedId, UUID userId, FeedUpdateRequest request) {
		Feed feed = feedRepository.findById(feedId).orElseThrow(
			() -> new EntityNotFoundException("feed not found. feed id: " + feedId)
		);

		feed.update(request.content());

		return feedDtoAssembler.assemble(feedId, userId);
	}

	//	TODO : @PreAuthorize() 사용자 관리 쪽 구현 후 완성하기
	@Transactional
	public void delete(UUID feedId) {
		ootdService.deleteAllByFeedId(feedId);
		commentService.deleteAllByFeedId(feedId);
		likeService.deleteAllByFeedId(feedId);

		feedCommentCountRepository.deleteById(feedId);
		feedLikeCountRepository.deleteById(feedId);

		feedRepository.deleteById(feedId);
	}

	@Transactional(readOnly = true)
	public FeedDtoCursorResponse getFeeds(UUID userId, FeedListRequest request) {

		List<Feed> feeds = feedRepositoryQueryDSL.getFeeds(request);
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
}
