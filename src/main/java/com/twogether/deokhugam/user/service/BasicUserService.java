package com.twogether.deokhugam.user.service;

import com.twogether.deokhugam.user.dto.UserDto;
import com.twogether.deokhugam.user.dto.UserLoginRequest;
import com.twogether.deokhugam.user.dto.UserRegisterRequest;
import com.twogether.deokhugam.user.entity.User;
import com.twogether.deokhugam.user.exception.EmailAlreadyExistsException;
import com.twogether.deokhugam.user.exception.InvalidCredentialsException;
import com.twogether.deokhugam.user.exception.NicknameAlreadyExistsException;
import com.twogether.deokhugam.user.mapper.UserMapper;
import com.twogether.deokhugam.user.repository.UserRepository;
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

        if (!user.getPassword().equals(password)) {
            throw InvalidCredentialsException.wrongPassword();
        }

        log.info("로그인 성공: userId={}, email={}", user.getId(), email);
        return userMapper.toDto(user);
    }
}
