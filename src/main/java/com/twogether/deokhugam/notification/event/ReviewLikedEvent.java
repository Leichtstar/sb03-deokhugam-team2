package com.twogether.deokhugam.notification.event;

import com.twogether.deokhugam.review.entity.Review;
import com.twogether.deokhugam.user.entity.User;

public record ReviewLikedEvent(User liker, Review review) {

}