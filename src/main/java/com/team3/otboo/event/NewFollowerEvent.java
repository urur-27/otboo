package com.team3.otboo.event;

import com.team3.otboo.domain.user.entity.User;

public record NewFollowerEvent(
    User followee,
    String followerName
) {

}
