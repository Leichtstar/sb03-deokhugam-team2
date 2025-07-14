package com.twogether.deokhugam.user.service;

import com.twogether.deokhugam.user.dto.UserDto;
import com.twogether.deokhugam.user.dto.UserRegisterRequest;

public interface UserService {
    UserDto create(UserRegisterRequest userRegisterRequest);
}
