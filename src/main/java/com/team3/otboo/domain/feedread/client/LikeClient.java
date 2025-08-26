package com.team3.otboo.domain.feedread.client;

import com.team3.otboo.domain.feed.service.LikeService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LikeClient {

	private final LikeService likeService;

	public Long count(UUID feedId) {
		return likeService.count(feedId);
	}
}
