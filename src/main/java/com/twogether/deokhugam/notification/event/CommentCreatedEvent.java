package com.twogether.deokhugam.notification.event;

import com.twogether.deokhugam.review.entity.Review;
import com.twogether.deokhugam.user.entity.User;

public record CommentCreatedEvent(User commenter, Review review, String commentContent) {

}