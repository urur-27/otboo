package com.team3.otboo.domain.feed.security;

import com.team3.otboo.domain.feed.repository.FeedRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("feedSecurity")
@RequiredArgsConstructor
public class FeedSecurity {

	private final FeedRepository feedRepository;

	public boolean isAuthor(UUID feedId, UUID userId) {
		return feedRepository.findAuthorIdById(feedId)
			.map(authorId -> authorId.equals(userId))
			.orElse(false);
	}
}
