package com.twogether.deokhugam.user.service;

import com.twogether.deokhugam.user.dto.UserDto;
import com.twogether.deokhugam.user.dto.UserRegisterRequest;
import com.twogether.deokhugam.user.entity.User;
import com.twogether.deokhugam.user.exception.UserAlreadyExistsException;
import com.twogether.deokhugam.user.mapper.UserMapper;
import com.twogether.deokhugam.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
            throw UserAlreadyExistsException.withEmail(email);
        }
        if (userRepository.existsByNickname(nickname)) {
            throw UserAlreadyExistsException.withNickname(nickname);
        }

        User user = new User(email, nickname, password);
        userRepository.save(user);

        log.info("사용자 생성 완료: id={}, nickname={}", user.getId(), nickname);

        return userMapper.toDto(user);
    }
}
