package com.team3.otboo.domain.user.dto.Request;

import com.team3.otboo.domain.user.entity.Location;
import com.team3.otboo.domain.user.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDate;

public record ProfileUpdateRequest(
        @NotBlank(message = "사용자 이름은 필수입니다!")
        @Size(min = 3, max = 20, message = "이름은 3자 이상, 20자 이하여야 합니다!")
        String name,

        Gender gender,

        LocalDate birthDate,

        Location location,

        @Range(min = 0, max = 5, message = "값은 0에서 5 사이여야 합니다!")
        Integer temperatureSensitivity
){

}
