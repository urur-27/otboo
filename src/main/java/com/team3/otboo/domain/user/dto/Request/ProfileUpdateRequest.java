package com.team3.otboo.domain.user.dto.Request;

import com.team3.otboo.domain.user.entity.Location;
import com.team3.otboo.domain.user.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDate;

public record ProfileUpdateRequest(
        String name,

        Gender gender,

        LocalDate birthDate,

        Location location,

        Integer temperatureSensitivity
){

}
