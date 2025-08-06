package com.team3.otboo.event;

import com.team3.otboo.domain.user.entity.User;
import java.util.UUID;

public record NewCommentEvent(
    User feedOwner,
    String commenterName,
    UUID feedId
) {

}
