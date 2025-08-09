package com.team3.otboo.domain.user.dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "사용자 이메일은 필수입니다!")
        @Email(message = "이메일 형식에 유효해야합니다!")
        @Size(max = 50, message = "이메일은 50자 이하여야 합니다!")
        String email
) {
}
