package com.twogether.deokhugam.user.controller;

import com.twogether.deokhugam.user.dto.UserDto;
import com.twogether.deokhugam.user.dto.UserLoginRequest;
import com.twogether.deokhugam.user.dto.UserRegisterRequest;
import com.twogether.deokhugam.user.dto.UserUpdateRequest;
import com.twogether.deokhugam.user.service.UserService;
import jakarta.validation.Valid;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping(path = "/{userId}")
    public ResponseEntity<UserDto> find(
        @PathVariable("userId") UUID userId
    ) {
        log.info("사용자 조회 요청: id={}", userId);

        UserDto user = userService.find(userId);

        log.debug("사용자 조회 응답: {}", user);

        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @PatchMapping(path = "/{userId}")
    public ResponseEntity<UserDto> update(
        @PathVariable("userId") UUID userId,
        @RequestBody @Valid UserUpdateRequest userUpdateRequest
    ) {
        log.info("사용자 수정 요청: id={}, request={}", userId, userUpdateRequest);

        UserDto updatedUser = userService.update(userId, userUpdateRequest);

        log.debug("사용자 수정 응답: {}", updatedUser);

        return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
    }
}
