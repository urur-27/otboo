package com.team3.otboo.event;

import com.team3.otboo.domain.user.entity.User;

public record DmReceivedEvent(
    User receiver,
    String senderName
) {

}
