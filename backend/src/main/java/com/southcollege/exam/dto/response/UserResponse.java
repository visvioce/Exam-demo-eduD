package com.southcollege.exam.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String role;

    private String status;

    private LocalDateTime createdAt;
}
