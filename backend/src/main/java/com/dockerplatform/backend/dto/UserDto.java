package com.dockerplatform.backend.dto;

import com.dockerplatform.backend.models.enums.BadgeType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String email;
    private String username;
    private String password;
    private BadgeType badge;
}
