package com.twogether.deokhugam.user.controller;

import com.twogether.deokhugam.user.dto.UserDto;
import com.twogether.deokhugam.user.dto.UserLoginRequest;
import com.twogether.deokhugam.user.dto.UserRegisterRequest;
import com.twogether.deokhugam.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> create(
        @RequestBody @Valid UserRegisterRequest userRegisterRequest
    ) {
        log.info("사용자 생성 요청: {}", userRegisterRequest);

        UserDto createdUser = userService.create(userRegisterRequest);

        log.debug("사용자 생성 응답: {}", createdUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping(path = "/login")
    public ResponseEntity<UserDto> login(
        @RequestBody @Valid UserLoginRequest userLoginRequest
    ) {
        log.info("로그인 요청: email={}", userLoginRequest.email());

        UserDto user = userService.login(userLoginRequest);

        log.debug("로그인 응답: {}", user);

        return ResponseEntity.status(HttpStatus.OK).body(user);
    }
}
