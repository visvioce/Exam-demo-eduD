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

/**
 * 用户实体
 * <p>表示系统中的用户（管理员、教师、学生），包含认证信息和基本信息。
 * 密码使用 BCrypt 加密存储，通过 {@code @JsonProperty(access = WRITE_ONLY)} 防止序列化泄露。</p>
 *
 * @see com.southcollege.exam.service.UserService
 * @see com.southcollege.exam.enums.RoleEnum
 */
@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名（唯一，3-20位字母数字下划线） */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    /** 密码（BCrypt加密存储，响应中不返回） */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100之间")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /** 显示昵称（最长100字符） */
    @Size(max = 100, message = "昵称长度不能超过100")
    private String nickname;

    /** 头像URL */
    private String avatar;

    /**
     * 用户角色
     * @see com.southcollege.exam.enums.RoleEnum
     */
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(ADMIN|TEACHER|STUDENT)$", message = "角色只能是 ADMIN、TEACHER 或 STUDENT")
    private String role;

    /**
     * 用户状态
     * @see com.southcollege.exam.enums.UserStatusEnum
     */
    private UserStatusEnum status;

    /** 注册/创建时间 */
    private LocalDateTime createdAt;

    /** 逻辑删除标记（0=未删除, 1=已删除） */
    @TableLogic
    private Integer deleted;
}
