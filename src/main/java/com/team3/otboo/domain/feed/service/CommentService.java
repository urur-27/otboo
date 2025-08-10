package com.team3.otboo.domain.feed.service;

import com.team3.otboo.domain.feed.dto.CommentDto;
import com.team3.otboo.domain.feed.dto.CommentDtoCursorResponse;
import com.team3.otboo.domain.feed.entity.Comment;
import com.team3.otboo.domain.feed.entity.FeedCommentCount;
import com.team3.otboo.domain.feed.mapper.CommentMapper;
import com.team3.otboo.domain.feed.repository.CommentRepository;
import com.team3.otboo.domain.feed.repository.FeedCommentCountRepository;
import com.team3.otboo.domain.feed.repository.FeedRepository;
import com.team3.otboo.domain.feed.service.request.CommentCreateRequest;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.enums.SortDirection;
import com.team3.otboo.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

	private final CommentRepository commentRepository;
	private final FeedCommentCountRepository feedCommentCountRepository;

	private final CommentMapper commentMapper;
	private final UserRepository userRepository;
	private final FeedRepository feedRepository;

	@Transactional
	public CommentDto create(CommentCreateRequest request) {
		if (!feedRepository.existsById(request.feedId())) {
			throw new IllegalArgumentException("feed not found. feed id: " + request.feedId());
		}

		User author = userRepository.findById(request.authorId()).orElseThrow(
			() -> new EntityNotFoundException("user not found. user Id: " + request.authorId())
		);

		Comment comment = commentRepository.save(Comment.create(
			request.feedId(),
			request.authorId(),
			request.content()
		));

		int result = feedCommentCountRepository.increase(request.feedId());
		if (result == 0) { // 객체 없으면 생성해서 넣어주기 .
			feedCommentCountRepository.save(
				FeedCommentCount.init(request.feedId(), 1L)
			);
		}

		return commentMapper.toDto(comment, author);
	}

	@Transactional
	public void delete(UUID commentId) {
		Comment comment = commentRepository.findById(commentId).orElseThrow(
			() -> new IllegalArgumentException("comment not found. commentId: " + commentId)
		);
		feedCommentCountRepository.decrease(comment.getFeedId());
		commentRepository.delete(comment);
	}

	@Transactional(readOnly = true)
	public CommentDtoCursorResponse getComments(UUID feedId, String cursor, UUID idAfter,
		int limit) {
		Long count = feedCommentCountRepository.findById(feedId)
			.map(FeedCommentCount::getCommentCount)
			.orElse(0L);

		int commentCount = count.intValue();

		Instant lastCreatedAt = null;
		if (cursor != null) {
			lastCreatedAt = Instant.parse(cursor);
		}

		List<Comment> comments = cursor == null || idAfter == null ?
			commentRepository.findAll(feedId, limit + 1) :
			commentRepository.findAll(feedId, lastCreatedAt, idAfter, limit + 1);

		boolean hasNext = comments.size() > limit;
		List<Comment> currentPage = hasNext ? comments.subList(0, limit) : comments;

		Set<UUID> authorIds = currentPage.stream()
			.map(Comment::getAuthorId)
			.collect(Collectors.toSet());

		Map<UUID, User> userMap = userRepository.findAllById(authorIds).stream()
			.collect(Collectors.toMap(User::getId, Function.identity()));

		List<CommentDto> commentDtoList = currentPage.stream()
			.map(comment -> {
				User user = userMap.get(comment.getAuthorId());
				if (user == null) {
					throw new EntityNotFoundException("User not found: " + comment.getAuthorId());
				}
				return commentMapper.toDto(comment, user);
			})
			.collect(Collectors.toList());

		String nextCursor = null;
		UUID nextIdAfter = null;

		if (hasNext && !currentPage.isEmpty()) {
			Comment lastElements = currentPage.getLast();
			nextCursor = lastElements.getCreatedAt().toString();
			nextIdAfter = lastElements.getId();
		}

		return new CommentDtoCursorResponse(commentDtoList, nextCursor, nextIdAfter, hasNext,
			commentCount, "createdAt", SortDirection.ASCENDING);
	}

	public void deleteAllByFeedId(UUID feedId) {
		commentRepository.deleteAllByFeedId(feedId);
	}

	// 테스트 데이터 생성용 ..
	@Transactional
	public Comment createBulk(CommentCreateRequest request) {
		Comment comment = commentRepository.save(Comment.create(
			request.feedId(),
			request.authorId(),
			request.content()
		));

		int result = feedCommentCountRepository.increase(request.feedId());
		if (result == 0) {
			feedCommentCountRepository.save(
				FeedCommentCount.init(request.feedId(), 1L)
			);
		}

		return comment;
	}
}
