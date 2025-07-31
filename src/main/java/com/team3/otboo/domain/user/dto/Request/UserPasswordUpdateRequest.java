package com.team3.otboo.domain.user.dto.Request;

import com.team3.otboo.domain.user.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserPasswordUpdateRequest(
        @NotBlank(message = "비밀번호는 필수입니다!")
        @Size(min = 8, max = 50, message = "비밀번호는 8자 이상, 50자 이하여야 합니다!")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).{8,}$",
                message = "비밀번호는 최소 8자 이상, 숫자, 문자, 특수문자를 포함해야 합니다")
        String newPassword
) {
}
