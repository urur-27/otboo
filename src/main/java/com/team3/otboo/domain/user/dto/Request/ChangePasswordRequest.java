package com.team3.otboo.domain.user.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "새 비밀번호를 반드시 입력해야 합니다!")
        @Size(min = 8, max = 50, message = "비밀번호는 8자 이상, 50자 이하여야 합니다!")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).{8,}$",
                message = "비밀번호는 최소 8자 이상, 숫자, 문자, 특수문자를 포함해야 합니다")
        String password
) {
}
