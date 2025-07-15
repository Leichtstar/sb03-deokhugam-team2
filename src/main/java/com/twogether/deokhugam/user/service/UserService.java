package com.twogether.deokhugam.user.service;

import com.twogether.deokhugam.user.dto.UserDto;
import com.twogether.deokhugam.user.dto.UserLoginRequest;
import com.twogether.deokhugam.user.dto.UserRegisterRequest;
import com.twogether.deokhugam.user.dto.UserUpdateRequest;
import java.util.UUID;

public interface UserService {
    UserDto create(UserRegisterRequest userRegisterRequest);

    UserDto login(UserLoginRequest userLoginRequest);

    UserDto find(UUID userId);

    UserDto update(UUID userId, UserUpdateRequest userUpdateRequest);
}
