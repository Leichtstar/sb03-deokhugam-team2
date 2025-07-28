package com.twogether.deokhugam.user.service;

import com.twogether.deokhugam.user.dto.UserDto;
import com.twogether.deokhugam.user.dto.UserLoginRequest;
import com.twogether.deokhugam.user.dto.UserRegisterRequest;
import com.twogether.deokhugam.user.dto.UserUpdateRequest;
import com.twogether.deokhugam.user.entity.User;
import com.twogether.deokhugam.user.exception.EmailAlreadyExistsException;
import com.twogether.deokhugam.user.exception.InvalidCredentialsException;
import com.twogether.deokhugam.user.exception.NicknameAlreadyExistsException;
import com.twogether.deokhugam.user.exception.UserNotFoundException;
import com.twogether.deokhugam.user.exception.UserWithdrawnException;
import com.twogether.deokhugam.user.mapper.UserMapper;
import com.twogether.deokhugam.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    @Override
    public UserDto create(UserRegisterRequest userRegisterRequest) {
        log.debug("사용자 생성 시작: {}", userRegisterRequest);

        String email = userRegisterRequest.email();
        String nickname = userRegisterRequest.nickname();
        String password = userRegisterRequest.password();

        if (userRepository.existsByEmail(email)) {
            throw EmailAlreadyExistsException.withEmail(email);
        }
        if (userRepository.existsByNickname(nickname)) {
            throw NicknameAlreadyExistsException.withNickname(nickname);
        }

        User user = new User(email, nickname, password);
        userRepository.save(user);

        log.info("사용자 생성 완료: id={}, nickname={}", user.getId(), nickname);

        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto login(UserLoginRequest userLoginRequest) {
        log.debug("로그인 시도: email={}", userLoginRequest.email());

        String email = userLoginRequest.email();
        String password = userLoginRequest.password();

        User user = userRepository.findByEmail(email)
            .orElseThrow(InvalidCredentialsException::emailNotFound);

        if (user.getIsDeleted().equals(true)) {
            throw UserWithdrawnException.withEmail(email);
        }

        if (!user.getPassword().equals(password)) {
            throw InvalidCredentialsException.wrongPassword();
        }

        log.info("로그인 성공: userId={}, email={}", user.getId(), email);
        return userMapper.toDto(user);
    }

    @Override
    public UserDto find(UUID userId) {
        log.debug("사용자 조회 시작: id={}", userId);
        UserDto userDto = userRepository.findById(userId)
            .map(userMapper::toDto)
            .orElseThrow(() -> UserNotFoundException.withId(userId));
        log.info("사용자 조회 완료: id={}", userId);
        return userDto;
    }

    @Transactional
    @Override
    public UserDto update(UUID userId, UserUpdateRequest userUpdateRequest) {
        log.debug("사용자 수정 시작: id={}, request={}", userId, userUpdateRequest);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                UserNotFoundException exception = UserNotFoundException.withId(userId);
                return exception;
            });

        String newNickname = userUpdateRequest.nickname();

        if (userRepository.existsByNickname(newNickname)) {
            throw NicknameAlreadyExistsException.withNickname(newNickname);
        }

        user.update(newNickname);

        log.info("사용자 수정 완료: id={}", userId);

        return userMapper.toDto(user);
    }

    @Transactional
    @Override
    public void softDelete(UUID userId) {
        log.debug("사용자 논리 삭제 시작: id={}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.withId(userId));

        user.softDelete(); // isDeleted = true로 설정

        log.info("사용자 논리 삭제 완료: id={}", userId);
    }

    @Transactional
    @Override
    public void hardDelete(UUID userId) {
        log.debug("사용자 물리 삭제 시작: id={}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.withId(userId));

        userRepository.delete(user);

        log.info("사용자 물리 삭제 완료: id={}", userId);
    }
}
