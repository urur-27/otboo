package com.team3.otboo.domain.follow.dto;

import com.team3.otboo.domain.user.dto.UserSummary;
import java.util.UUID;

public record FollowDto(
	UUID id,
	UserSummary followee,
	UserSummary follower
) {

}
