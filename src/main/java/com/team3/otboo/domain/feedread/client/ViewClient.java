package com.team3.otboo.domain.feedread.client;

import com.team3.otboo.domain.feed.service.ViewService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ViewClient {

	private final ViewService viewService;

	public Long count(UUID feedId) {
		return viewService.count(feedId);
	}
}
