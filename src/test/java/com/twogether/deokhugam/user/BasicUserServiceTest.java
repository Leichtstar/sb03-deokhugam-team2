package com.twogether.deokhugam.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.twogether.deokhugam.user.dto.UserDto;
import com.twogether.deokhugam.user.dto.UserRegisterRequest;
import com.twogether.deokhugam.user.entity.User;
import com.twogether.deokhugam.user.exception.UserAlreadyExistsException;
import com.twogether.deokhugam.user.mapper.UserMapper;
import com.twogether.deokhugam.user.repository.UserRepository;
import com.twogether.deokhugam.user.service.BasicUserService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class BasicUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private BasicUserService userService;

    private UUID userId;
    private String email;
    private String nickname;
    private String password;
    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        email = "test@example.com";
        nickname = "testUser";
        password = "password123";

        user = new User(email, nickname, password);
        ReflectionTestUtils.setField(user, "id", userId);
        userDto = new UserDto(userId, email, nickname, Instant.now());
    }

    @Test
    @DisplayName("사용자 생성 성공")
    void createUser_Success() {
        // given
        UserRegisterRequest request = new UserRegisterRequest(email, nickname, password);
        given(userRepository.existsByEmail(eq(email))).willReturn(false);
        given(userRepository.existsByNickname(eq(nickname))).willReturn(false);
        given(userMapper.toDto(any(User.class))).willReturn(userDto);

        // when
        UserDto result = userService.create(request);

        // then
        assertThat(result).isEqualTo(userDto);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 사용자 생성 시도 시 실패")
    void createUser_WithExistingEmail_ThrowsException() {
        // given
        UserRegisterRequest request = new UserRegisterRequest(email, nickname, password);
        given(userRepository.existsByEmail(eq(email))).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.create(request))
            .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    @DisplayName("이미 존재하는 닉네임으로 사용자 생성 시도 시 실패")
    void createUser_WithExistingUsername_ThrowsException() {
        // given
        UserRegisterRequest request = new UserRegisterRequest(email, nickname, password);
        given(userRepository.existsByEmail(eq(email))).willReturn(false);
        given(userRepository.existsByNickname(eq(nickname))).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.create(request))
            .isInstanceOf(UserAlreadyExistsException.class);
    }
}
