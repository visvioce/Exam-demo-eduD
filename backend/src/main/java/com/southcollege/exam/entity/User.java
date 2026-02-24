package com.southcollege.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.southcollege.exam.enums.UserStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    private String avatar;

    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(ADMIN|TEACHER|STUDENT)$", message = "角色只能是 ADMIN、TEACHER 或 STUDENT")
    private String role;

    private UserStatusEnum status;

    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}
