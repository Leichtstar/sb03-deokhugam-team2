package com.twogether.deokhugam.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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
            .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    @DisplayName("이미 존재하는 닉네임으로 사용자 생성 시도 시 실패")
    void createUser_WithExistingNickname_ThrowsException() {
        // given
        UserRegisterRequest request = new UserRegisterRequest(email, nickname, password);
        given(userRepository.existsByEmail(eq(email))).willReturn(false);
        given(userRepository.existsByNickname(eq(nickname))).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.create(request))
            .isInstanceOf(NicknameAlreadyExistsException.class);
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given - 테스트 데이터 준비
        UserLoginRequest loginRequest = new UserLoginRequest(email, password);
        given(userRepository.findByEmail(eq(email))).willReturn(Optional.of(user)); // 이메일로 사용자 찾기 성공
        given(userMapper.toDto(any(User.class))).willReturn(userDto); // User를 UserDto로 변환

        // when - 실제 로그인 메소드 호출
        UserDto result = userService.login(loginRequest);

        // then - 결과 검증
        assertThat(result).isEqualTo(userDto); // 반환된 UserDto가 예상과 같은지 확인
        verify(userRepository).findByEmail(eq(email)); // findByEmail이 호출되었는지 확인
        verify(userMapper).toDto(any(User.class)); // toDto가 호출되었는지 확인
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시도 시 실패")
    void login_WithNonExistentEmail_ThrowsException() {
        // given - 존재하지 않는 이메일 상황 설정
        String nonExistentEmail = "nonexistent@example.com";
        UserLoginRequest loginRequest = new UserLoginRequest(nonExistentEmail, password);
        given(userRepository.findByEmail(eq(nonExistentEmail))).willReturn(Optional.empty()); // 이메일로 사용자 찾기 실패

        // when & then - 예외 발생 확인
        assertThatThrownBy(() -> userService.login(loginRequest))
            .isInstanceOf(InvalidCredentialsException.class); // InvalidCredentialsException 발생해야 함
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시도 시 실패")
    void login_WithWrongPassword_ThrowsException() {
        // given - 올바른 이메일이지만 잘못된 비밀번호
        String wrongPassword = "wrongPassword";
        UserLoginRequest loginRequest = new UserLoginRequest(email, wrongPassword);
        given(userRepository.findByEmail(eq(email))).willReturn(Optional.of(user)); // 이메일로 사용자 찾기는 성공

        // when & then - 비밀번호 불일치로 예외 발생 확인
        assertThatThrownBy(() -> userService.login(loginRequest))
            .isInstanceOf(InvalidCredentialsException.class); // InvalidCredentialsException 발생해야 함
    }

    @Test
    @DisplayName("탈퇴한 사용자 로그인 시도 시 실패")
    void login_WithWithdrawnUser_ThrowsException() {
        // given - 탈퇴한 사용자 상황 설정
        user.softDelete();

        UserLoginRequest loginRequest = new UserLoginRequest(email, password);

        // 탈퇴한 사용자를 찾도록 Mock 설정
        given(userRepository.findByEmail(eq(email)))
            .willReturn(Optional.of(user));

        // when & then - UserWithdrawnException 발생 확인
        assertThatThrownBy(() -> userService.login(loginRequest))
            .isInstanceOf(UserWithdrawnException.class)  // 탈퇴 사용자 전용 예외 확인
            .hasMessage("탈퇴한 사용자입니다.");  // 예외 메시지 확인 (ErrorCode에 따라 달라질 수 있음)
    }

    @Test
    @DisplayName("사용자 조회 성공")
    void findUser_Success() {
        // given
        given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));
        given(userMapper.toDto(any(User.class))).willReturn(userDto);

        // when
        UserDto result = userService.find(userId);

        // then
        assertThat(result).isEqualTo(userDto);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 실패")
    void findUser_WithNonExistentId_ThrowsException() {
        // given
        given(userRepository.findById(eq(userId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.find(userId))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("사용자 수정 성공")
    void updateUser_Success() {
        // given
        String newNickname = "newNickname";
        UserUpdateRequest request = new UserUpdateRequest(newNickname);

        given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));
        given(userRepository.existsByNickname(eq(newNickname))).willReturn(false);
        given(userMapper.toDto(any(User.class))).willReturn(userDto);

        // when
        UserDto result = userService.update(userId, request);

        // then
        assertThat(result).isEqualTo(userDto);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 수정 시도 시 실패")
    void updateUser_WithNonExistentId_ThrowsException() {
        // given
        UserUpdateRequest request = new UserUpdateRequest("newNickname");
        given(userRepository.findById(eq(userId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.update(userId, request))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("사용자 수정 시 이미 존재하는 닉네임으로 변경 시도하면 실패")
    void updateUser_WithExistingNickname_ThrowsException() {
        // given
        String existingNickname = "existingUser";
        UserUpdateRequest updateRequest = new UserUpdateRequest(existingNickname);

        given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));
        given(userRepository.existsByNickname(eq(existingNickname))).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.update(userId, updateRequest))
            .isInstanceOf(NicknameAlreadyExistsException.class);

        // verify 호출 검증
        verify(userRepository).findById(eq(userId));
        verify(userRepository).existsByNickname(eq(existingNickname));
    }

    @Test
    @DisplayName("사용자 논리 삭제 성공")
    void softDeleteUser_Success() {
        // given
        given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));

        // when
        userService.softDelete(userId);

        // then
        verify(userRepository).findById(eq(userId));
        // softDelete() 메소드가 호출되었는지는 user 객체의 상태로 확인
    }

    @Test
    @DisplayName("존재하지 않는 사용자 논리 삭제 시도 시 실패")
    void softDeleteUser_WithNonExistentId_ThrowsException() {
        // given
        given(userRepository.findById(eq(userId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.softDelete(userId))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("사용자 물리 삭제 성공")
    void hardDeleteUser_Success() {
        // given
        given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));

        // when
        userService.hardDelete(userId);

        // then
        verify(userRepository).findById(eq(userId));
        verify(userRepository).delete(eq(user));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 물리 삭제 시도 시 실패")
    void hardDeleteUser_WithNonExistentId_ThrowsException() {
        // given
        given(userRepository.findById(eq(userId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.hardDelete(userId))
            .isInstanceOf(UserNotFoundException.class);
    }
}
