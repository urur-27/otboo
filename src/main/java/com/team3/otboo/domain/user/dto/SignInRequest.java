package com.team3.otboo.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignInRequest(
        @NotBlank(message = "사용자 이메일은 필수입니다!")
        @Email(message = "이메일 형식에 유효해야합니다!")
        @Size(max = 50, message = "이메일은 50자 이하여야 합니다!")
        String email,
        @NotBlank(message = "비밀번호는 필수입니다!")
        @Size(min = 8, max = 50, message = "비밀번호는 8자 이상, 50자 이하여야 합니다!")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).{8,}$",
                message = "비밀번호는 최소 8자 이상, 숫자, 문자, 특수문자를 포함해야 합니다")
        String password
) {
}
