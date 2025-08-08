package com.team3.otboo.domain.feed.comment.service;

import com.team3.otboo.domain.feed.mapper.CommentMapper;
import com.team3.otboo.domain.feed.repository.CommentRepository;
import com.team3.otboo.domain.feed.repository.FeedCommentCountRepository;
import com.team3.otboo.domain.feed.repository.FeedRepository;
import com.team3.otboo.domain.feed.service.CommentService;
import com.team3.otboo.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

	@Mock
	private FeedRepository feedRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private CommentRepository commentRepository;

	@Mock
	private FeedCommentCountRepository feedCommentCountRepository;

	@Mock
	private CommentMapper commentMapper;

	@InjectMocks
	private CommentService commentService;

	@Test
	void createTest() {

	}
}
