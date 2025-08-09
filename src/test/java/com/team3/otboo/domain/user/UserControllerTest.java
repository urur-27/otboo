package com.team3.otboo.domain.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3.otboo.domain.user.controller.UserController;
import com.team3.otboo.domain.user.dto.Request.UserCreateRequest;
import com.team3.otboo.domain.user.dto.response.UserResponse;
import com.team3.otboo.domain.user.enums.Role;
import com.team3.otboo.domain.user.service.UserService;
import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UserService userService;

    private UserCreateRequest userCreateRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        // @MockBean 대신 직접 생성
        userService = Mockito.mock(UserService.class);
        userCreateRequest = new UserCreateRequest("TestUser", "test@naver.com", "password");
        userResponse = UserResponse.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .email(userCreateRequest.email())
                .name(userCreateRequest.name())
                .role(Role.USER)
                .linkedOAuthProviders(Collections.emptySet())
                .locked(false)
                .build();
    }

    @Test
    @DisplayName("회원가입 성공")
    public void signup_success() throws Exception {
        // given
        given(userService.createUser(any(UserCreateRequest.class)))
                .willReturn(userResponse);

        // when
        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateRequest)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn();

        // then
        String jsonResponse = result.getResponse().getContentAsString();
        UserResponse response = objectMapper.readValue(jsonResponse, UserResponse.class);

        // assertAll
        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.getId()).isNotNull(),
                () -> assertThat(response.getEmail()).isEqualTo("test@naver.com"),
                () -> assertThat(response.getName()).isEqualTo("TestUser"),
                () -> assertThat(response.getRole()).isEqualTo(Role.USER),
                () -> assertThat(response.isLocked()).isFalse()
        );
    }

    @Test
    @DisplayName("회원가입 실패, 이미 존재하는 이메일로 회원가입 시도")
    public void signup_fail_email() throws Exception {
        // given
        given(userService.createUser(any(UserCreateRequest.class)))
                .willThrow(new BusinessException(ErrorCode.ALREADY_EXISTS, "이미 존재하는 이메일입니다."));

        // when then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 존재하는 이메일입니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 실패, 이미 존재하는 이름으로 회원가입 시도")
    public void signup_fail_name()  throws Exception {
        // given
        given(userService.createUser(any(UserCreateRequest.class)))
                .willThrow(new BusinessException(ErrorCode.ALREADY_EXISTS, "이미 존재하는 이름입니다."));

        // when / then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 존재하는 이름입니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 실패, 정보 누락")
    public void signup_fail_Input_is_not_enough() throws Exception {
        // given
        // 필수 정보 누락 = 이름
        UserCreateRequest invalidRequest = new UserCreateRequest(null, "invalid-email", "invalid-password");

        // when
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("필수 정보가 누락됐습니다."))
                .andDo(print());
    }
}
