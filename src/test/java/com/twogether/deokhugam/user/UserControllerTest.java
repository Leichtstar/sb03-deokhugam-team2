package com.twogether.deokhugam.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twogether.deokhugam.user.controller.UserController;
import com.twogether.deokhugam.user.dto.UserDto;
import com.twogether.deokhugam.user.dto.UserUpdateRequest;
import com.twogether.deokhugam.user.service.UserService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private String email;
    private String nickname;
    private String password;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        email = "test@example.com";
        nickname = "testUser";
        password = "password123";
        userDto = new UserDto(userId, email, nickname, Instant.now());
    }

    @Test
    @DisplayName("사용자 수정 실패 - 헤더 누락")
    void updateUser_WithoutHeader_ThrowsAccessDeniedException() throws Exception {
        // given
        UserUpdateRequest request = new UserUpdateRequest("newNickname");

        // when & then
        mockMvc.perform(patch("/api/users/{userId}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                // 헤더 없이 요청
            )
            .andExpect(status().isForbidden()); // 403 Forbidden
    }

    @Test
    @DisplayName("사용자 수정 실패 - 잘못된 헤더 형식")
    void updateUser_WithInvalidHeaderFormat_ThrowsAccessDeniedException() throws Exception {
        // given
        UserUpdateRequest request = new UserUpdateRequest("newNickname");
        String invalidUserId = "invalid-uuid-format";

        // when & then
        mockMvc.perform(patch("/api/users/{userId}", userId)
                .header("Deokhugam-Request-User-ID", invalidUserId) // 잘못된 형식
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden()); // 403 Forbidden
    }

    @Test
    @DisplayName("사용자 수정 실패 - 다른 사용자 ID")
    void updateUser_WithDifferentUserId_ThrowsAccessDeniedException() throws Exception {
        // given
        UUID differentUserId = UUID.randomUUID();
        UserUpdateRequest request = new UserUpdateRequest("newNickname");

        // when & then
        mockMvc.perform(patch("/api/users/{userId}", userId)
                .header("Deokhugam-Request-User-ID", differentUserId.toString()) // 다른 사용자 ID
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden()); // 403 Forbidden
    }

}
