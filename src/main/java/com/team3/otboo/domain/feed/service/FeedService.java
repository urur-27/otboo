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
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedService {

	private final FeedRepository feedRepository;
	private final FeedDtoAssembler feedDtoAssembler;

	private final OotdService ootdService;
	private final CommentService commentService;
	private final LikeService likeService;

	private final FeedLikeCountRepository feedLikeCountRepository;
	private final FeedRepositoryQueryDSL feedRepositoryQueryDSL;
	private final FeedCommentCountRepository feedCommentCountRepository;

	// 알림 기능과 겹치니까 publish 는 한번만 하고, Listener 를 두개 써야함 .
	// 겹치는거 -> 좋아요 생성 삭제, 댓글 생성 시 알림 가야하고 + 인기 피드 쪽에서 점수 계산까지 해야함 .

	@Transactional
	public FeedDto create(UUID userId, FeedCreateRequest request) {

		Feed feed = feedRepository.save(Feed.create(
			request.authorId(),
			request.weatherId(),
			request.content())
		);

		ootdService.create(feed.getId(), request.clothesIds());

		return feedDtoAssembler.assemble(feed.getId(), userId);
	}

	@PreAuthorize("@feedSecurity.isAuthor(#feedId, authentication.principal.id)")
	@Transactional
	public FeedDto update(UUID feedId, UUID userId, FeedUpdateRequest request) {
		Feed feed = feedRepository.findById(feedId).orElseThrow(
			() -> new EntityNotFoundException("feed not found. feed id: " + feedId)
		);

		feed.update(request.content());

		return feedDtoAssembler.assemble(feedId, userId);
	}

	@PreAuthorize("@feedSecurity.isAuthor(#feedId, authentication.principal.id)")
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
}
