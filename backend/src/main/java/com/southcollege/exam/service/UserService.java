package com.southcollege.exam.service;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.southcollege.exam.dto.response.LoginResponse;
import com.southcollege.exam.dto.response.UserResponse;
import com.southcollege.exam.entity.User;
import com.southcollege.exam.exception.BusinessException;
import com.southcollege.exam.mapper.UserMapper;
import com.southcollege.exam.enums.UserStatusEnum;
import com.southcollege.exam.utils.JwtUtil;
import com.southcollege.exam.utils.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务
 * 负责用户登录、注册、密码管理等认证相关功能
 */
@Slf4j
@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 根据用户名查询用户
     */
    public User getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    /**
     * 构建用户显示名映射（优先昵称，其次用户名）
     */
    public Map<Long, String> getDisplayNameMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return listByIds(userIds).stream()
                .collect(Collectors.toMap(
                        User::getId,
                        this::getDisplayName,
                        (a, _b) -> a
                ));
    }

    /**
     * 获取单个用户显示名（优先昵称，其次用户名）
     */
    public String getDisplayName(User user) {
        if (user == null) {
            return "";
        }
        if (StringUtils.isNotBlank(user.getNickname())) {
            return user.getNickname();
        }
        return user.getUsername() == null ? "" : user.getUsername();
    }

    /**
     * 用户登录
     * 验证用户名密码，生成JWT Token
     */
    public LoginResponse login(String username, String password) {
        // 查询用户
        User user = getByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 验证密码（BCrypt加密比较）
        if (!PasswordUtil.matches(password, user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        // 检查用户状态
        if (user.getStatus() != UserStatusEnum.ACTIVE) {
            throw new BusinessException("用户已被禁用");
        }

        // 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        log.info("用户登录成功: username={}, userId={}", username, user.getId());

        // 构建登录响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(convertToResponse(user));
        return response;
    }

    /**
     * 用户注册
     * 密码加密存储
     */
    @Transactional
    public UserResponse register(String username, String password, String nickname, String role) {
        // 检查用户名是否已存在
        if (getByUsername(username) != null) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.encrypt(password)); // BCrypt加密
        user.setNickname(nickname);
        user.setRole(role);
        user.setStatus(UserStatusEnum.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());

        save(user);
        log.info("用户注册成功: username={}, userId={}", username, user.getId());
        return convertToResponse(user);
    }

    /**
     * 获取当前用户信息
     */
    public UserResponse getCurrentUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToResponse(user);
    }

    /**
     * 修改密码
     * 需要验证原密码
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 验证原密码
        if (!PasswordUtil.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 更新为新密码（加密存储）
        user.setPassword(PasswordUtil.encrypt(newPassword));
        updateById(user);
        log.info("用户修改密码成功: userId={}", userId);
    }

    /**
     * 更新个人资料（当前登录用户）
     */
    @Transactional
    public UserResponse updateProfile(Long userId, String nickname, String avatar) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setNickname(nickname);
        user.setAvatar(avatar);
        updateById(user);
        return convertToResponse(user);
    }

    /**
     * 创建用户（管理员使用）
     * 密码加密存储
     */
    @Transactional
    public boolean createUser(User user) {
        // 检查用户名是否已存在
        if (getByUsername(user.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        
        // 密码加密
        user.setPassword(PasswordUtil.encrypt(user.getPassword()));
        
        // 设置默认状态
        if (user.getStatus() == null) {
            user.setStatus(UserStatusEnum.ACTIVE);
        }
        
        // 设置创建时间
        user.setCreatedAt(LocalDateTime.now());
        
        return save(user);
    }
    
    /**
     * 更新用户信息（管理员使用）
     * 不允许直接修改密码，密码修改需通过 changePassword 方法
     */
    @Transactional
    public boolean updateUser(User user) {
        User existingUser = getById(user.getId());
        if (existingUser == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 保留原密码，不允许通过此接口修改密码
        user.setPassword(existingUser.getPassword());
        
        return updateById(user);
    }

    /**
     * 将User实体转换为UserResponse（隐藏敏感信息）
     */
    private UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }
}
