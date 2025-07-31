package com.team3.otboo.domain.feed.mapper;

import com.team3.otboo.domain.feed.dto.CommentDto;
import com.team3.otboo.domain.feed.entity.Comment;
import com.team3.otboo.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentMapper {

	private final AuthorMapper authorMapper;

	public CommentDto toDto(Comment comment, User author) {
		return new CommentDto(
			comment.getId(),
			comment.getCreatedAt(),
			comment.getFeedId(),
			authorMapper.toDto(author),
			comment.getContent()
		);
	}
}
