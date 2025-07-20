package com.twogether.deokhugam.user.controller;

import com.twogether.deokhugam.user.dto.UserDto;
import com.twogether.deokhugam.user.dto.UserLoginRequest;
import com.twogether.deokhugam.user.dto.UserRegisterRequest;
import com.twogether.deokhugam.user.dto.UserUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "사용자 관리", description = "사용자 관련 API")
public interface UserApi {

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", description = "회원가입 성공",
            content = @Content(schema = @Schema(implementation = UserDto.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                        {
                            "timestamp": "2025-07-20T10:30:00.123456Z",
                            "code": "INVALID_INPUT_VALUE",
                            "message": "잘못된 입력값입니다.",
                            "details": {
                                "password": "비밀번호는 8자 이상 20자 이하, 영문자, 숫자, 특수문자를 포함해야 합니다."
                            },
                            "exceptionType": "MethodArgumentNotValidException",
                            "status": 400
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409", description = "이메일/닉네임 중복",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "EmailAlreadyExistsException",
                        value = """
                            {
                                "timestamp": "2025-07-20T10:30:00.123456Z",
                                "code": "EMAIL_DUPLICATION",
                                "message": "이미 존재하는 이메일입니다.",
                                "details": {
                                    "email": "test@example.com"
                                },
                                "exceptionType": "EmailAlreadyExistsException",
                                "status": 409
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "NicknameAlreadyExistsException",
                        value = """
                            {
                                "timestamp": "2025-07-20T10:30:00.123456Z",
                                "code": "NICKNAME_DUPLICATION",
                                "message": "이미 존재하는 닉네임입니다.",
                                "details": {
                                    "nickname": "test"
                                },
                                "exceptionType": "NicknameAlreadyExistsException",
                                "status": 409
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500", description = "서버 내부 오류",
            content = @Content(examples = @ExampleObject(value = "서버 내부 오류: null"))
        )
    })
    ResponseEntity<UserDto> create(
        @Parameter(
            description = "회원가입 정보",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        ) UserRegisterRequest userRegisterRequest
    );

    @Operation(summary = "로그인", description = "사용자 로그인을 처리합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = UserDto.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                        {
                            "timestamp": "2025-07-20T10:30:00.123456Z",
                            "code": "INVALID_INPUT_VALUE",
                            "message": "잘못된 입력값입니다.",
                            "details": {
                                "password": "비밀번호는 8자 이상 20자 이하, 영문자, 숫자, 특수문자를 포함해야 합니다."
                            },
                            "exceptionType": "MethodArgumentNotValidException",
                            "status": 400
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", description = "로그인 실패 (이메일 또는 비밀번호 불일치)",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                        {
                            "timestamp": "2025-07-20T10:30:00.123456Z",
                            "code": "LOGIN_INPUT_INVALID",
                            "message": "이메일 또는 비밀번호가 올바르지 않습니다.",
                            "exceptionType": "InvalidCredentialsException",
                            "status": 401
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500", description = "서버 내부 오류",
            content = @Content(examples = @ExampleObject(value = "서버 내부 오류: null"))
        )
    })
    ResponseEntity<UserDto> login(
        @Parameter(
            description = "로그인 정보",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        ) UserLoginRequest userLoginRequest
    );

    @Operation(summary = "사용자 정보 조회", description = "사용자 ID로 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "사용자 정보 조회 성공",
            content = @Content(schema = @Schema(implementation = UserDto.class))
        ),
        @ApiResponse(
            responseCode = "404", description = "사용자 정보 없음",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                        {
                            "timestamp": "2025-07-20T10:30:00.123456Z",
                            "code": "USER_NOT_FOUND",
                            "message": "사용자를 찾을 수 없습니다.",
                            "details": {
                                "userId": "1527a5de-1274-4e4e-bbe1-0e119030981b"
                            },
                            "exceptionType": "UserNotFoundException",
                            "status": 404
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500", description = "서버 내부 오류",
            content = @Content(examples = @ExampleObject(value = "서버 내부 오류: null"))
        )
    })
    ResponseEntity<UserDto> find(
        @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000") UUID userId
    );

    @Operation(summary = "사용자 논리 삭제", description = "사용자를 논리적으로 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", description = "사용자 삭제 성공"
        ),
        @ApiResponse(
            responseCode = "403", description = "사용자 삭제 권한 없음",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "userIdMismatch",
                        value = """
                        {
                            "timestamp": "2025-07-20T10:30:00.123456Z",
                            "code": "USER_ACCESS_DENIED",
                            "message": "사용자 정보 수정 권한이 없습니다.",
                            "details": {
                                "requestUserId": "a244bc95-f26a-4814-8c4d-a050f0dbde11",
                                "targetUserId": "b244bc95-f26a-4814-8c4d-a050f0dbde10"
                            },
                            "exceptionType": "UserAccessDeniedException",
                            "status": 403
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "missingUserIdHeader",
                        value = """
                        {
                            "timestamp": "2025-07-20T10:30:00.123456Z",
                            "code": "USER_ACCESS_DENIED",
                            "message": "사용자 정보 수정 권한이 없습니다.",
                            "details": {
                                "missingUserIdHeader": ""
                            },
                            "exceptionType": "UserAccessDeniedException",
                            "status": 403
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "invalidUserIdFormat",
                        value = """
                        {
                            "timestamp": "2025-07-20T10:30:00.123456Z",
                            "code": "USER_ACCESS_DENIED",
                            "message": "사용자 정보 수정 권한이 없습니다.",
                            "details": {
                                "invalidUserId": "a3dc2482697c4124bcbfee0a706c0e6"
                            },
                            "exceptionType": "UserAccessDeniedException",
                            "status": 403
                        }
                        """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "404", description = "사용자 정보 없음",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                        {
                            "timestamp": "2025-07-20T10:30:00.123456Z",
                            "code": "USER_NOT_FOUND",
                            "message": "사용자를 찾을 수 없습니다.",
                            "details": {
                                "userId": "1527a5de-1274-4e4e-bbe1-0e119030981b"
                            },
                            "exceptionType": "UserNotFoundException",
                            "status": 404
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500", description = "서버 내부 오류",
            content = @Content(examples = @ExampleObject(value = "서버 내부 오류: null"))
        )
    })
    ResponseEntity<Void> softDelete(
        @Parameter(description = "삭제할 사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000") UUID userId,
        @Parameter(
            name = "Deokhugam-Request-User-ID",
            description = "요청을 보낸 사용자 ID (인증 확인용)",
            in = ParameterIn.HEADER,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) String requestUserIdHeader
    );

    @Operation(summary = "사용자 정보 수정", description = "사용자의 닉네임을 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "사용자 정보 수정 성공",
            content = @Content(schema = @Schema(implementation = UserDto.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                        {
                            "timestamp": "2025-07-20T10:30:00.123456Z",
                            "code": "INVALID_INPUT_VALUE",
                            "message": "잘못된 입력값입니다.",
                            "details": {
                                "password": "비밀번호는 8자 이상 20자 이하, 영문자, 숫자, 특수문자를 포함해야 합니다."
                            },
                            "exceptionType": "MethodArgumentNotValidException",
                            "status": 400
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403", description = "사용자 정보 수정 권한 없음",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "userIdMismatch",
                        value = """
                        {
                            "timestamp": "2025-07-20T10:30:00.123456Z",
                            "code": "USER_ACCESS_DENIED",
                            "message": "사용자 정보 수정 권한이 없습니다.",
                            "details": {
                                "requestUserId": "a244bc95-f26a-4814-8c4d-a050f0dbde11",
                                "targetUserId": "b244bc95-f26a-4814-8c4d-a050f0dbde10"
                            },
                            "exceptionType": "UserAccessDeniedException",
                            "status": 403
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "missingUserIdHeader",
                        value = """
                        {
                            "timestamp": "2025-07-20T10:30:00.123456Z",
                            "code": "USER_ACCESS_DENIED",
                            "message": "사용자 정보 수정 권한이 없습니다.",
                            "details": {
                                "missingUserIdHeader": ""
                            },
                            "exceptionType": "UserAccessDeniedException",
                            "status": 403
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "invalidUserIdFormat",
                        value = """
                        {
                            "timestamp": "2025-07-20T10:30:00.123456Z",
                            "code": "USER_ACCESS_DENIED",
                            "message": "사용자 정보 수정 권한이 없습니다.",
                            "details": {
                                "invalidUserId": "a3dc2482697c4124bcbfee0a706c0e6"
                            },
                            "exceptionType": "UserAccessDeniedException",
                            "status": 403
                        }
                        """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "404", description = "사용자 정보 없음",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                        {
                            "timestamp": "2025-07-20T10:30:00.123456Z",
                            "code": "USER_NOT_FOUND",
                            "message": "사용자를 찾을 수 없습니다.",
                            "details": {
                                "userId": "1527a5de-1274-4e4e-bbe1-0e119030981b"
                            },
                            "exceptionType": "UserNotFoundException",
                            "status": 404
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500", description = "서버 내부 오류",
            content = @Content(examples = @ExampleObject(value = "서버 내부 오류: null"))
        )
    })
    ResponseEntity<UserDto> update(
        @Parameter(description = "수정할 사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000") UUID userId,
        @Parameter(
            description = "수정할 사용자 정보",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        ) UserUpdateRequest userUpdateRequest,
        @Parameter(
            name = "Deokhugam-Request-User-ID",
            description = "요청을 보낸 사용자 ID (인증 확인용)",
            in = ParameterIn.HEADER,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) String requestUserIdHeader
    );

    @Operation(summary = "사용자 물리 삭제", description = "사용자를 물리적으로 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", description = "사용자 삭제 성공"
        ),
        @ApiResponse(
            responseCode = "403", description = "사용자 삭제 권한 없음",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "userIdMismatch",
                        value = """
                        {
                            "timestamp": "2025-07-20T10:30:00.123456Z",
                            "code": "USER_ACCESS_DENIED",
                            "message": "사용자 정보 수정 권한이 없습니다.",
                            "details": {
                                "requestUserId": "a244bc95-f26a-4814-8c4d-a050f0dbde11",
                                "targetUserId": "b244bc95-f26a-4814-8c4d-a050f0dbde10"
                            },
                            "exceptionType": "UserAccessDeniedException",
                            "status": 403
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "missingUserIdHeader",
                        value = """
                        {
                            "timestamp": "2025-07-20T10:30:00.123456Z",
                            "code": "USER_ACCESS_DENIED",
                            "message": "사용자 정보 수정 권한이 없습니다.",
                            "details": {
                                "missingUserIdHeader": ""
                            },
                            "exceptionType": "UserAccessDeniedException",
                            "status": 403
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "invalidUserIdFormat",
                        value = """
                        {
                            "timestamp": "2025-07-20T10:30:00.123456Z",
                            "code": "USER_ACCESS_DENIED",
                            "message": "사용자 정보 수정 권한이 없습니다.",
                            "details": {
                                "invalidUserId": "a3dc2482697c4124bcbfee0a706c0e6"
                            },
                            "exceptionType": "UserAccessDeniedException",
                            "status": 403
                        }
                        """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "404", description = "사용자 정보 없음",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                        {
                            "timestamp": "2025-07-20T10:30:00.123456Z",
                            "code": "USER_NOT_FOUND",
                            "message": "사용자를 찾을 수 없습니다.",
                            "details": {
                                "userId": "1527a5de-1274-4e4e-bbe1-0e119030981b"
                            },
                            "exceptionType": "UserNotFoundException",
                            "status": 404
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500", description = "서버 내부 오류",
            content = @Content(examples = @ExampleObject(value = "서버 내부 오류: null"))
        )
    })
    ResponseEntity<Void> hardDelete(
        @Parameter(description = "삭제할 사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000") UUID userId,
        @Parameter(
            name = "Deokhugam-Request-User-ID",
            description = "요청을 보낸 사용자 ID (인증 확인용)",
            in = ParameterIn.HEADER,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) String requestUserIdHeader
    );
}
