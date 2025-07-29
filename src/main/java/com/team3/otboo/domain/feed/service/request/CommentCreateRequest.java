package com.team3.otboo.domain.feed.service.request;

import java.util.UUID;
import org.antlr.v4.runtime.misc.NotNull;

public record CommentCreateRequest(
	UUID feedId,
	UUID authorId,
	String content
) {

}
